/*
 *  Copyright 2015 LG CNS.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package scouter.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import scouter.client.net.INetReader;
import scouter.client.net.TcpProxy;
import scouter.client.server.ServerManager;
import scouter.client.util.ConsoleProxy;
import scouter.io.DataInputX;
import scouter.lang.counters.CounterEngine;
import scouter.lang.pack.ObjectPack;
import scouter.lang.value.ListValue;
import scouter.net.RequestCmd;
import scouter.util.ThreadUtil;

public class AgentModelThread extends Thread {

	private static AgentModelThread instance = null;

	public final static synchronized AgentModelThread getInstance() {
		if (instance == null) {
			instance = new AgentModelThread();
			instance.setDaemon(true);
			instance.setName(ThreadUtil.getName(instance));
			instance.start();
		}
		return instance;
	}

	private Map<Integer, AgentObject> agentMap = new HashMap<Integer, AgentObject>();
	
	ArrayList<ObjectPack> allAgentList = new ArrayList<ObjectPack>();
	
	private boolean existUnknownType;
	
	public ArrayList<ObjectPack> getAgentPackList(){
		return allAgentList;
	}
	
	public void run() {
		while (brun) {
			fetchObjectList();
			for (int i = 0; i < 20 && brun; i++) {
				ThreadUtil.sleep(100);
			}
		}
	}

	public synchronized void fetchObjectList() {
		Map<Integer, AgentObject> tempAgentMap = new HashMap<Integer, AgentObject>();
		ArrayList<ObjectPack> objectPackList = new ArrayList<ObjectPack>();
		boolean existUnknownType = false;
		Set<Integer> serverIdSet = ServerManager.getInstance().getOpenServerList();
		if (serverIdSet.size() > 0) {
			Integer[] serverIds = serverIdSet.toArray(new Integer[serverIdSet.size()]);
			for (int serverId : serverIds) {
				CounterEngine counterEngine = ServerManager.getInstance().getServer(serverId).getCounterEngine();
				TcpProxy proxy = TcpProxy.getTcpProxy(serverId);
				try {
					final ArrayList<ObjectPack> agentList = new ArrayList<ObjectPack>();
					proxy.process(RequestCmd.OBJECT_LIST_REAL_TIME, null, new INetReader() {
						public void process(DataInputX in) throws IOException {
							ObjectPack o = (ObjectPack) in.readPack();
							agentList.add(o);
						}
					});
					objectPackList.addAll(agentList);
					for (int i = 0; agentList != null && i < agentList.size(); i++) {
						ObjectPack m = (ObjectPack) agentList.get(i);
						String objType = m.objType;
						int objHash = m.objHash;
						String objName = m.objName;
					    if (TextProxy.object.getText(objHash) == null) {
					    	TextProxy.object.putText(objHash, objName);
					    }
						AgentObject agentObject = new AgentObject(objType, objHash, objName, serverId);
						tempAgentMap.put(objHash, agentObject);
						agentObject.objPack = m;
						if (counterEngine.isUnknownObjectType(objType)) {
							existUnknownType = true;
						}
					}
				} catch (Exception e) {
					ConsoleProxy.errorSafe(e.toString());
				} finally {
					TcpProxy.putTcpProxy(proxy);
				}
			}
		}
		
		allAgentList = objectPackList;
		agentMap = tempAgentMap;
		this.existUnknownType = existUnknownType;
	}
	
	public AgentObject getAgentObject(int objHash) {
		return agentMap.get(objHash);
	}
	
	public Map<Integer, AgentObject> getAgentObjectMap() {
		return agentMap;
	}
	
	public static void removeInactive() {
		Set<Integer> serverIdSet = ServerManager.getInstance().getOpenServerList();
		if (serverIdSet.size() > 0) {
			Integer[] serverIds = serverIdSet.toArray(new Integer[serverIdSet.size()]);
			for (int serverId : serverIds) {
				TcpProxy proxy = TcpProxy.getTcpProxy(serverId);
				try {
					proxy.process(RequestCmd.OBJECT_REMOVE_INACTIVE, null);
				} finally {
					TcpProxy.putTcpProxy(proxy);
				}
			}
		}
	}
	
	public Set<Integer> getObjectSetByServerId (int serverId) {
		Set<Integer> set = new HashSet<Integer>();
		for (AgentObject obj : agentMap.values()) {
			if (serverId == obj.getServerId()) {
				set.add(obj.objHash);
			}
		}
		return set;
	}
	
	public AgentObject[] getObjectList () {
		return agentMap.values().toArray(new AgentObject[agentMap.size()]);
	}
	
	public Set<String> getCurrentObjTypeSet() {
		Set<String> typeSet = new HashSet<String>();
		Iterator<Integer> keys = agentMap.keySet().iterator();
		while (keys.hasNext()) {
			AgentObject agent = agentMap.get(keys.next());
			typeSet.add(agent.getObjType());
		}
		return typeSet;
	}
	
	public boolean existUnknownType() {
		return this.existUnknownType;
	}
	
	public Set<Integer> getObjectList(String objType) {
		Set<Integer> set = new HashSet<Integer>();
		Iterator<Integer> keys = agentMap.keySet().iterator();
		while (keys.hasNext()) {
			AgentObject agent = agentMap.get(keys.next());
			if(objType.equals(agent.getObjType())) {
				set.add(agent.getObjHash());
			}
		}
		return set;
	}
	
	public ListValue getObjHashLV(int serverId, String objType) {
		ListValue lv = new ListValue();
		for (AgentObject obj : agentMap.values()) {
			if (serverId == obj.getServerId() && objType.equals(obj.getObjType()) ) {
				lv.add(obj.objHash);
			}
		}
		return lv;
	}
	
	public Set<String> getCurrentObjectTypeList(int serverId) {
		Set<String> objectTypeList = new TreeSet<String>(); 
		for (AgentObject agent : agentMap.values()) {
			if (agent.getServerId() == serverId) {
				String objType = agent.getObjType();
				objectTypeList.add(objType);
			}
		}
		return objectTypeList;
	}
	
	public boolean existFamilyType(int serverId, String family) {
		CounterEngine engine = ServerManager.getInstance().getServer(serverId).getCounterEngine();
		for (AgentObject agent : agentMap.values()) {
			if (agent.getServerId() == serverId && engine.isChildOf(agent.getObjType(), family)) {
				return true;
			}
		}
		return false;
	}

	private boolean brun = true;

	public void shutdown() {
		brun = false;
	}

}

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
 */

package scouter.net;

import scouter.io.DataInputX;


public class NetCafe {
	public final static int CAFE_LENGTH = 4;
	public final static byte[] JAVA = "JAVA".getBytes();
	public final static byte[] JAVAN = "JAVN".getBytes();
	public final static byte[] JS01 = "JS01".getBytes();
	public final static byte[] NODE = "NODE".getBytes();
	public final static byte[] NODEN = "NODN".getBytes(); 
	public final static byte[] JMTU = "JMTU".getBytes();
	//
	public final static int UDP_JAVA = 0x4a415641;
	public final static int UDP_JAVAN = 0x4a41564e;
	public final static int UDP_NODE = 0x4e4f4445;
	public final static int UDP_NODEN = 0x4e4f444e;
	public final static int UDP_JMTU = 0x4a4d5455;
	//
	public static final int TCP_AGENT_DATA =0xCAFE1001;
	public static final int TCP_AGENT_REQUEST =0xCAFE1002;
	public static final int TCP_AGENT_RESPONSE =0xCAFE1003;
	public static final int TCP_CLIENT_REQUEST = 0xCAFE1004;
	public static final int TCP_CLIENT_RESPONSE = 0xCAFE1005;
	
	public static final int TCP_SHUTDOWN = 0xCAFE1999;
	
	public static void main(String[] args) {
		
		System.out.println("JAVA = 0x" +Integer.toHexString(DataInputX.toInt(JAVA, 0)));
		System.out.println("JAVAN = 0x" +Integer.toHexString(DataInputX.toInt(JAVAN, 0)));
		System.out.println("NODE = 0x" +Integer.toHexString(DataInputX.toInt(NODE, 0)));
		System.out.println("NODEN = 0x" +Integer.toHexString(DataInputX.toInt(NODEN, 0)));
		System.out.println("JMTU = 0x" +Integer.toHexString(DataInputX.toInt(JMTU, 0)));
		}

}
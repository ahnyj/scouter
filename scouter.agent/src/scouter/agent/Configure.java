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

package scouter.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import scouter.Version;
import scouter.agent.netio.data.DataProxy;
import scouter.lang.conf.ConfObserver;
import scouter.lang.conf.ConfigValueUtil;
import scouter.lang.counters.CounterConstants;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;
import scouter.net.NetConstants;
import scouter.util.DateUtil;
import scouter.util.FileUtil;
import scouter.util.HashUtil;
import scouter.util.IntSet;
import scouter.util.ParamText;
import scouter.util.StringEnumer;
import scouter.util.StringKeyLinkedMap;
import scouter.util.StringSet;
import scouter.util.StringUtil;
import scouter.util.SysJMX;
import scouter.util.SystemUtil;
import scouter.util.ThreadUtil;

public class Configure extends Thread {

	public static boolean JDBC_REDEFINED = false;

	// public static int MAX_PACKET_SIZE = 60000;

	private static Configure instance = null;

	public final static synchronized Configure getInstance() {
		if (instance == null) {
			instance = new Configure();
			instance.setDaemon(true);
			instance.setName(ThreadUtil.getName(instance));
			instance.start();
		}
		return instance;
	}

	public boolean debug_config = false;

	public String local_addr = null;
	public int local_port;

	public String tcp_addr = null;
	public int tcp_port = NetConstants.BASE_TCP_AGENT_PORT;
	public int tcp_port_max = NetConstants.BASE_TCP_AGENT_PORT + 100;

	public String server_addr = "127.0.0.1";
	public int server_port = NetConstants.DATAUDP_SERVER_PORT;

	public String scouter_type = "";
	public String scouter_name = "";
	public String scouter_host_type = "";
	public String scouter_hostname = "";
	public int objHash;
	public String objName;

	public int objHostHash;
	public String objHostName;
	public boolean enable_host_agent = false;

	public boolean enable_scouter_name_pid = false;
	public boolean enable_plus_objtype = false;

	public boolean enable_asm_jdbc = true;
	public boolean enable_asm_httpsession = true;
	public boolean enable_asm_socket = true;

	public boolean http_debug_querystring;
	public boolean http_debug_header;
	public String http_debug_header_url = "/";
	public boolean http_debug_parameter;
	public String http_debug_parameter_url = "/";

	/*
	 * visitor축정방법 0 - remoteIp 1 - JSESSIONID + remoteIp 2 -
	 * SCOUTER(set-cookie)
	 */
	public int mode_visitor = 1;

	public boolean enable_profile_summary = false;
	public boolean profile_thread_cputime = false;
	public boolean profile_socket_openstack = false;
	public boolean debug_socket_openstack = false;
	public int profile_socket_openstack_port = 0;
	public int debug_socket_openstack_port = 0;

	public int xlog_time_limit = 0;

	public String http_error_status = "";
	private IntSet http_error_status_set = new IntSet();

	public String service_header_key;
	public String service_get_key;
	public String service_post_key;

	public File dump_dir = new File(".");
	public File subagent_dir = new File("./_scouter_");

	public boolean enable_auto_dump = false;
	public int auto_dump_trigger = 10000;
	public long auto_dump_interval = 30000;
	public int auto_dump_level = 1;

	public String http_static_contents = "js, htm, html, gif, png, jpg, css";
	private Set<String> static_contents = new HashSet<String>();

	public int alert_message_length = 3000;
	public long alert_send_interval = 3000;
	public int alert_fetch_count = 100000;
	public int alert_sql_time = 30000;

	public int udp_packet_max = 60000;
	public boolean debug_asm;
	public boolean debug_udp_xlog;
	public boolean debug_udp_object;

	public long yellow_line_time = 3000;
	public long red_line_time = 8000;

	public long delayed_time = 10000;

	public String plugin_classpath = "";

	public StringSet log_ignore = new StringSet();

	public String hook_args = "";
	public String hook_return = "";
	public String hook_init = "";
	public String hook_dbc_close = "";
	public String hook_dbc_open = "";

	public String hook_method = "";
	public String hook_method_ignore_prefix = "";
	private String[] ignore_prefix = null;
	private int ignore_prefix_len = 0;
	public boolean hook_method_access_public = true;
	public boolean hook_method_access_private = false;
	public boolean hook_method_access_protected = false;
	public boolean hook_method_access_none = false;

	public String hook_service = "";
	public String hook_apicall = "";
	public String hook_apicall_info = "";
	public String hook_jsp = "";

	// /LOAD CONTROL/////
	public boolean enable_reject_service = false;
	public int max_active_service = 10000;
	public boolean enable_reject_url = false;
	public String reject_text = "too many request!!";
	public String reject_url = "/error.html";

	public int profile_step_max = 1024;
	// public long udp_collection_interval=200;

	public boolean debug_background_sql = false;

	public String plugin_http_trace = "";
	public String plugin_apicall_name = "";
	public String plugin_http_trace_param = "";
	public boolean profile_fullstack_service_error = false;
	public boolean profile_fullstack_apicall_error = false;
	public int profile_fullstack_lines = 0;
	public long udp_collection_interval = 100;

	public String http_remote_ip_header_key = "";
	public boolean enable_trace_e2e = false;
	public String gxid_key = "gxid";
	public boolean enable_response_gxid = false;
	public String scouter_this_txid = "scouter_this_txid";
	public String scouter_caller_txid = "scouter_caller_txid";

	public int hook_signature;

	public int max_concurrent_server_request = 10;
	public String visitor_jsessionid = "JSESSIONID";
	public int statistics_interval = (int) DateUtil.MILLIS_PER_FIVE_MINUTE;
	public boolean enable_auto_service_trace = false;

	public boolean debug_apicall = false;

	public boolean debug_connection_stack = false;
	public boolean enable_statistics = true;
	public String hook_future_task = "";
	public String hook_future_task_prefix = "";

	public boolean enable_counter_task = true;
	public boolean enable_hook_step1 = true;
	public boolean enable_hook_step2 = true;
	public boolean enable_hook_step3 = true;
	public boolean enable_hook_step4 = true;
	public boolean enable_hook_step5 = true;
	public boolean enable_hook_step6 = true;
	public boolean enable_hook_step7 = true;
	public boolean enable_hook_step8 = true;

	public int stat_sql_max = 10000;
	public int stat_api_max = 5000;
	public int stat_app_sql_max = 10000;
	public int stat_app_api_max = 5000;

	public String direct_patch_class = "";

	/**
	 * sometimes call by sample application, at that time normally set some
	 * properties directly
	 */
	private Configure() {
		Properties p = new Properties();
		Map args = new HashMap();
		args.putAll(System.getenv());
		args.putAll(System.getProperties());
		p.putAll(args);
		this.property = p;
		reload(false);
	}

	private Configure(boolean b) {
	}

	private long last_load_time = -1;
	public Properties property = new Properties();

	private boolean running = true;

	public void run() {
		Logger.info("Version " + Version.getAgentFullVersion());
		long dateUnit = DateUtil.getDateUnit();
		while (running) {
			reload(false);
			// Text Data Reset..
			long nowUnit = DateUtil.getDateUnit();
			if (dateUnit != nowUnit) {
				dateUnit = nowUnit;
				DataProxy.reset();
			}
			ThreadUtil.sleep(3000);
		}
	}

	private File propertyFile;

	public File getPropertyFile() {
		if (propertyFile != null) {
			return propertyFile;
		}
		String s = System.getProperty("scouter.config", "./scouter.conf");
		propertyFile = new File(s.trim());
		return propertyFile;
	}

	long last_check = 0;

	public synchronized boolean reload(boolean force) {
		long now = System.currentTimeMillis();
		if (force == false && now < last_check + 3000)
			return false;
		last_check = now;

		File file = getPropertyFile();

		if (file.lastModified() == last_load_time) {
			return false;
		}

		last_load_time = file.lastModified();
		Properties temp = new Properties();

		if (file.canRead()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				temp.load(in);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				FileUtil.close(in);
			}
		}
		property = ConfigValueUtil.replaceSysProp(temp);
		apply();
		ConfObserver.run();
		return true;
	}

	private void apply() {
		this.debug_config = getBoolean("debug_config", getBoolean("debug.config", false));
		this.http_debug_querystring = getBoolean("http_debug_querystring", false);
		this.http_debug_header = getBoolean("http_debug_header", false);
		this.http_debug_parameter = getBoolean("http_debug_parameter", false);
		this.enable_profile_summary = getBoolean("enable_profile_summary", getBoolean("enable.profile.summary", false));
		this.xlog_time_limit = getInt("xlog_time_limit", getInt("xlog.time.limit", 0));
		this.service_header_key = getValue("service_header_key", getValue("service.header.key", null));
		this.service_get_key = getValue("service_get_key");
		this.service_post_key = getValue("service_post_key");

		this.http_error_status = getValue("http_error_status", getValue("http.error.status", ""));

		this.dump_dir = new File(getValue("dump_dir", getValue("dump.dir", ".")));
		try {
			this.dump_dir.mkdirs();
		} catch (Exception e) {
		}
		this.subagent_dir = new File(getValue("subagent_dir", getValue("subagent.dir", "./_scouter_")));
		try {
			this.subagent_dir.mkdirs();
		} catch (Exception e) {
		}

		this.enable_auto_dump = getBoolean("enable_auto_dump", getBoolean("enable.auto.dump", false));
		this.auto_dump_trigger = getInt("auto_dump_trigger", getInt("auto.dump.trigger", 10000));
		if (this.auto_dump_trigger < 1) {
			this.auto_dump_trigger = 1;
		}
		this.auto_dump_level = getInt("auto_dump_level", getInt("auto.dump.level", 1));
		this.auto_dump_interval = getInt("auto_dump_interval", getInt("auto.dump.interval", 30000));
		if (this.auto_dump_interval < 5000) {
			this.auto_dump_interval = 5000;
		}

		this.http_static_contents = getValue("http_static_contents",
				getValue("http.static.contents", "js, htm, html, gif, png, jpg, css"));

		this.profile_thread_cputime = getBoolean("profile_thread_cputime", getBoolean("profile.thread.cputime", false));
		this.profile_socket_openstack = getBoolean("profile_socket_openstack",
				getBoolean("profile.socket.openstack", false));
		this.debug_socket_openstack = getBoolean("debug_socket_openstack", getBoolean("debug.socket.openstack", false));
		this.profile_socket_openstack_port = getInt("profile_socket_openstack_port", 0);
		this.debug_socket_openstack_port = getInt("debug_socket_openstack_port", 0);

		this.enable_asm_jdbc = getBoolean("enable_asm_jdbc", getBoolean("enable.asm.jdbc", true));
		this.enable_asm_httpsession = getBoolean("enable_asm_httpsession", getBoolean("enable.asm.httpsession", true));
		this.enable_asm_socket = getBoolean("enable_asm_socket", getBoolean("enable.asm.socket", true));

		this.udp_packet_max = getInt("udp_packet_max", getInt("udp.packet.max", 60000));

		this.yellow_line_time = getLong("yellow_line_time", getLong("yellow.line.time", 3000));
		this.red_line_time = getLong("red_line_time", getLong("red.line.time", 8000));

		this.delayed_time = getLong("delayed_time", getLong("delayed.time", 10000));

		this.log_ignore = getStringSet("log_ignore", ",");

		this.debug_udp_xlog = getBoolean("debug_udp_xlog", getBoolean("debug.udp.xlog", false));
		this.debug_udp_object = getBoolean("debug_udp_object", getBoolean("debug.udp.object", false));

		this.local_addr = getValue("local_addr", getValue("local.addr"));
		this.local_port = getInt("local_port", getInt("local.port", 0));

		this.tcp_addr = getValue("tcp_addr", getValue("tcp.addr"));
		this.tcp_port = getInt("tcp_port", getInt("tcp.port", NetConstants.BASE_TCP_AGENT_PORT));
		this.tcp_port_max = getInt("tcp_port_max", getInt("tcp.port.max", this.tcp_port + 100));

		this.server_addr = getValue("server_addr", getValue("server.addr", "127.0.0.1"));
		this.server_port = getInt("server_port", getInt("server.port", NetConstants.DATAUDP_SERVER_PORT));

		this.hook_signature = 0;
		this.hook_args = getValue("hook_args", getValue("hook.args", ""));
		this.hook_return = getValue("hook_return", getValue("hook.return", ""));
		this.hook_init = getValue("hook_init", getValue("hook.init", ""));
		this.hook_dbc_close = getValue("hook_dbc_close", getValue("hook.dbc.close", ""));
		this.hook_dbc_open = getValue("hook_dbc_open", getValue("hook.dbc.open", ""));

		this.hook_method = getValue("hook_method", getValue("hook.method", ""));
		this.hook_method_access_public = getBoolean("hook_method_access_public", true);
		this.hook_method_access_protected = getBoolean("hook_method_access_protected", false);
		this.hook_method_access_private = getBoolean("hook_method_access_private", false);
		this.hook_method_access_none = getBoolean("hook_method_access_none", false);
		this.hook_method_ignore_prefix = StringUtil.removeWhitespace(getValue("hook_method_ignore_prefix", "get,set"));
		this.ignore_prefix = StringUtil.split(this.hook_method_ignore_prefix, ",");
		this.ignore_prefix_len = this.ignore_prefix == null ? 0 : this.ignore_prefix.length;

		this.hook_service = getValue("hook_service", getValue("hook.service", ""));
		this.hook_apicall = getValue("hook_apicall", getValue("hook.subcall", ""));
		this.hook_apicall_info = getValue("hook_apicall_info", "");
		this.hook_jsp = getValue("hook_jsp", getValue("hook.jsp", ""));

		this.hook_signature ^= this.hook_args.hashCode();
		this.hook_signature ^= this.hook_return.hashCode();
		this.hook_signature ^= this.hook_init.hashCode();
		this.hook_signature ^= this.hook_dbc_close.hashCode();
		this.hook_signature ^= this.hook_dbc_open.hashCode();
		this.hook_signature ^= this.hook_method.hashCode();
		this.hook_signature ^= this.hook_service.hashCode();
		this.hook_signature ^= this.hook_apicall.hashCode();
		this.hook_signature ^= this.hook_jsp.hashCode();

		this.plugin_classpath = getValue("plugin_classpath", "");

		this.enable_reject_service = getBoolean("enable_reject_service", false);
		this.max_active_service = getInt("max_active_service", 10000);
		this.enable_reject_url = getBoolean("enable_reject_url", false);
		this.reject_text = getValue("reject_text", "too many request!!");
		this.reject_url = getValue("reject_url", "/error.html");

		this.profile_step_max = getInt("profile_step_max", 1024);
		if (this.profile_step_max < 100)
			this.profile_step_max = 100;

		this.debug_background_sql = getBoolean("debug_background_sql", false);

		this.plugin_http_trace = getValue("plugin_http_trace", "");
		this.plugin_apicall_name = getValue("plugin_apicall_name", "");

		this.profile_fullstack_service_error = getBoolean("profile_fullstack_service_error", false);
		this.profile_fullstack_apicall_error = getBoolean("profile_fullstack_apicall_error", false);
		this.profile_fullstack_lines = getInt("profile_fullstack_lines", 0);

		this.udp_collection_interval = getInt("udp_collection_interval", 100);
		this.http_debug_parameter_url = getValue("http_debug_parameter_url", "/");
		this.http_debug_header_url = getValue("http_debug_header_url", "/");

		this.http_remote_ip_header_key = getValue("http_remote_ip_header_key", "");

		this.enable_trace_e2e = getBoolean("enable_trace_e2e", getBoolean("enable_gxid", false));
		this.enable_response_gxid = getBoolean("enable_response_gxid", false);
		this.gxid_key = getValue("gxid_key", "gxid");
		this.scouter_this_txid = getValue("scouter_this_txid", "scouter_this_txid");
		this.scouter_caller_txid = getValue("scouter_caller_txid", "scouter_caller_txid");

		this.max_concurrent_server_request = getInt("max_concurrent_server_request", 10);
		this.debug_connection_stack = getBoolean("debug_connection_stack", false);

		this.mode_visitor = getInt("mode_visitor", 1);

		this.visitor_jsessionid = getValue("visitor_jsessionid", "JSESSIONID");
		this.enable_statistics = getBoolean("enable_statistics", true);
		this.statistics_interval = getInt("statistics_interval", (int) DateUtil.MILLIS_PER_FIVE_MINUTE);

		this.enable_host_agent = getBoolean("enable_host_agent", false);
		this.enable_auto_service_trace = getBoolean("enable_auto_service_trace", false);
		this.debug_apicall = getBoolean("debug_apicall", false);

		this.hook_future_task = getValue("hook_future_task", "");
		this.hook_future_task_prefix = getValue("hook_future_task_prefix", "");

		this.enable_counter_task = getBoolean("enable_counter_task", true);
		this.enable_hook_step1 = getBoolean("enable_hook_step1", true);
		this.enable_hook_step2 = getBoolean("enable_hook_step2", true);
		this.enable_hook_step3 = getBoolean("enable_hook_step3", true);
		this.enable_hook_step4 = getBoolean("enable_hook_step4", true);
		this.enable_hook_step5 = getBoolean("enable_hook_step5", true);
		this.enable_hook_step6 = getBoolean("enable_hook_step6", true);
		this.enable_hook_step7 = getBoolean("enable_hook_step7", true);
		this.enable_hook_step8 = getBoolean("enable_hook_step8", true);

		this.stat_sql_max = getInt("stat_sql_max", 10000);
		this.stat_api_max = getInt("stat_api_max", 5000);
		this.stat_app_sql_max = getInt("stat_app_sql_max", 10000);
		this.stat_app_api_max = getInt("stat_app_api_max", 5000);

		this.plugin_http_trace_param = getValue("plugin_http_trace_param", "");

		this.direct_patch_class = getValue("direct_patch_class", "");

		resetObjInfo();
		setErrorStatus();
		setStaticContents();
	}

	private StringSet getStringSet(String key, String deli) {
		StringSet set = new StringSet();
		String v = getValue(key);
		if (v != null) {
			String[] vv = StringUtil.split(v, deli);
			for (String x : vv) {
				x = StringUtil.trimToEmpty(x);
				if (x.length() > 0)
					set.put(x);
			}
		}
		return set;
	}

	private void setStaticContents() {
		Set<String> tmp = new HashSet<String>();
		String[] s = StringUtil.split(this.http_static_contents, ',');
		for (int i = 0; i < s.length; i++) {
			String ss = s[i].trim();
			if (ss.length() > 0) {
				tmp.add(ss);
			}
		}
		static_contents = tmp;
	}

	public boolean isStaticContents(String content) {
		return static_contents.contains(content);
	}

	public boolean isIgnoreMethodPrefix(String name) {
		for (int i = 0; i < this.ignore_prefix_len; i++) {
			if (name.startsWith(this.ignore_prefix[i]))
				return true;
		}
		return false;
	}

	public synchronized void resetObjInfo() {
		String detected = ObjTypeDetector.drivedType != null ? ObjTypeDetector.drivedType
				: ObjTypeDetector.objType != null ? ObjTypeDetector.objType : CounterConstants.JAVA;

		this.scouter_type = getValue("scouter_type", getValue("scouter.type", detected));

		detected = CounterConstants.HOST;
		if (SystemUtil.IS_LINUX) {
			detected = CounterConstants.LINUX;
		} else if (SystemUtil.IS_WINDOWS) {
			detected = CounterConstants.WINDOWS;
		} else if (SystemUtil.IS_MAC_OSX) {
			detected = CounterConstants.OSX;
		} else if (SystemUtil.IS_AIX) {
			detected = CounterConstants.AIX;
		} else if (SystemUtil.IS_HP_UX) {
			detected = CounterConstants.HPUX;
		}
		this.scouter_host_type = getValue("scouter_host_type", getValue("scouter.host.type", detected));
		this.scouter_hostname = getValue("scouter_hostname", getValue("scouter.hostname", SysJMX.getHostName()));

		this.objHostName = "/" + this.scouter_hostname;
		this.objHostHash = HashUtil.hash(objHostName);

		this.enable_scouter_name_pid = getBoolean("enable_scouter_name_pid", false);
		String defaultName;
		if (this.enable_scouter_name_pid == true) {
			defaultName = "" + SysJMX.getProcessPID();
		} else {
			defaultName = this.scouter_type + "1";
		}
		this.scouter_name = getValue("scouter_name",
				getValue("scouter.name", System.getProperty("jvmRoute", defaultName)));

		this.objName = objHostName + "/" + this.scouter_name;
		this.objHash = HashUtil.hash(objName);

		this.alert_message_length = getInt("alert_message_length", getInt("alert.message.length", 3000));
		this.alert_send_interval = getInt("alert_send_interval", getInt("alert.send.interval", 3000));
		this.alert_fetch_count = getInt("alert_fetch_count", getInt("alert.fetch.count", 100000));
		this.alert_sql_time = getInt("alert_sql_time", getInt("alert.sql.time", 30000));

		System.setProperty("scouter.object.name", this.objName);
		System.setProperty("scouter.object.type", this.scouter_type);

		this.debug_asm = getBoolean("debug_asm", getBoolean("debug.asm", false));

		this.enable_plus_objtype = getBoolean("enable_plus_objtype", false);

	}

	private void setErrorStatus() {
		String[] status = StringUtil.split(this.http_error_status, ',');
		http_error_status_set.clear();
		for (int i = 0; i < status.length; i++) {
			try {
				int code = Integer.parseInt(status[i].trim());
				if (code > 0) {
					http_error_status_set.add(code);
				}
			} catch (Exception e) {
			}
		}
	}

	public boolean isErrorStatus(int status) {
		return http_error_status_set.contains(status);
	}

	public String getValue(String key) {
		return StringUtil.trim(property.getProperty(key));
	}

	public String getValue(String key, String def) {
		return StringUtil.trim(property.getProperty(key, def));
	}

	public int getInt(String key, int def) {
		try {
			String v = getValue(key);
			if (v != null)
				return Integer.parseInt(v);
		} catch (Exception e) {
		}
		return def;
	}

	public long getLong(String key, long def) {
		try {
			String v = getValue(key);
			if (v != null)
				return Long.parseLong(v);
		} catch (Exception e) {
		}
		return def;
	}

	public boolean getBoolean(String key, boolean def) {
		try {
			String v = getValue(key);
			if (v != null)
				return Boolean.parseBoolean(v);
		} catch (Exception e) {
		}
		return def;
	}

	public String loadText() {
		File file = getPropertyFile();
		InputStream fin = null;
		try {
			fin = new FileInputStream(file);
			byte[] buff = FileUtil.readAll(fin);
			return new String(buff);
		} catch (Exception e) {
		} finally {
			FileUtil.close(fin);
		}
		return null;
	}

	public boolean saveText(String text) {
		File file = getPropertyFile();
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(text.getBytes());
			return true;
		} catch (Exception e) {
		} finally {
			FileUtil.close(out);
		}
		return false;
	}

	public void printConfig() {
		Logger.info("Configure -Dscouter.config=" + propertyFile );
	}

	private static HashSet<String> ignoreSet = new HashSet<String>();

	static {
		ignoreSet.add("property");
		ignoreSet.add("objHash");
		ignoreSet.add("objHostHash");
		ignoreSet.add("objHostName");
		ignoreSet.add("objName");
		ignoreSet.add("objType");
	}

	public MapValue getKeyValueInfo() {
		StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(new Configure(true));
		StringKeyLinkedMap<Object> curMap = ConfigValueUtil.getConfigDefault(this);
		MapValue m = new MapValue();
		ListValue nameList = m.newList("key");
		ListValue valueList = m.newList("value");
		ListValue defList = m.newList("default");

		StringEnumer enu = defMap.keys();
		while (enu.hasMoreElements()) {
			String key = enu.nextString();
			if (ignoreSet.contains(key))
				continue;
			nameList.add(key);
			valueList.add(ConfigValueUtil.toValue(curMap.get(key)));
			defList.add(ConfigValueUtil.toValue(defMap.get(key)));
		}

		return m;
	}

	public static void main(String[] args) {
		System.out.println(Configure.getInstance().getKeyValueInfo().toString().replace(',', '\n'));
	}
}
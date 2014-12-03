package org.ethereum.jsontestsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ethereum.vm.LogInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

public class Logs {
	Map<byte[], LogInfo> logs;
	
	public Logs(JSONObject jLogs) {
		logs = new HashMap<byte[], LogInfo>();
		
		Set keys = jLogs.keySet();
		for(Object k: keys.toArray()) {
			byte[] key = Hex.decode((String)k);
			
			JSONObject values = (JSONObject)jLogs.get(k);
			
			byte[] address = Hex.decode((String)values.get("address"));
			byte[] data = Hex.decode(((String)values.get("data")).substring(2));
			List<byte[]> topics = new ArrayList<byte[]>();
			
			JSONArray jTopics = (JSONArray)values.get("topics");
			for(Object t: jTopics.toArray()) {
				byte[] topic = Hex.decode(((String)t));
				topics.add(topic);
			}
			
			LogInfo li = new LogInfo(address, topics, data);
			logs.put(key, li);
		}
	}
	
	/**
	 * returns null if {@link org.ethereum.vm.LogInfo LogInfo} object was not found for the given key
	 * @param k
	 * @return
	 */
	public LogInfo getLogBloom(byte[] k) {
		if(logs.containsKey(k))
			return logs.get(k);
		return null;
	}
	public Iterator<byte[]> getLogsRLPSHA3KeyIterator() {
		return logs.keySet().iterator();
	}
}

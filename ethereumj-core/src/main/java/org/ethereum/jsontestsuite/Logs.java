package org.ethereum.jsontestsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

public class Logs {
	List<LogInfo> logs = new ArrayList<>();
	
	public Logs(JSONArray jLogs) {

        for (int i = 0; i < jLogs.size(); ++i){

            JSONObject jLog = (JSONObject)jLogs.get(i);
            byte[] address = Hex.decode((String)jLog.get("address"));
            byte[] data =    Hex.decode(((String)jLog.get("data")).substring(2));

            List<DataWord> topics = new ArrayList<>();

            JSONArray jTopics = (JSONArray)jLog.get("topics");
            for(Object t: jTopics.toArray()) {
                byte[] topic = Hex.decode(((String)t));
                topics.add(new DataWord(topic));
            }

            LogInfo li = new LogInfo(address, topics, data);
            logs.add(li);
        }
	}


    public Iterator<LogInfo> getIterator(){
        return logs.iterator();
    }
}

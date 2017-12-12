/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Logs {
    List<LogInfo> logs;
    byte[] logsHash;

    @SuppressWarnings("unchecked")
    public Logs(Object logs) {
        if (logs instanceof JSONArray) {
            init((JSONArray) logs);
        } else if (logs instanceof List) {
            this.logs = (List<LogInfo>) logs;
        } else {
            init((String) logs);
        }
    }

    public static class Deserializer extends JsonDeserializer<Logs> {
        @Override
        public Logs deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return new Logs(jp.readValueAs(Object.class));
        }
    }

    private void init(String logsHash) {
        this.logsHash = Utils.parseData(logsHash);
    }

    private void init(JSONArray jLogs) {
        logs = new ArrayList<>();
        for (Object jLog1 : jLogs) {

            JSONObject jLog = (JSONObject) jLog1;
            byte[] address = Hex.decode((String) jLog.get("address"));
            byte[] data = Hex.decode(((String) jLog.get("data")).substring(2));

            List<DataWord> topics = new ArrayList<>();

            JSONArray jTopics = (JSONArray) jLog.get("topics");
            for (Object t : jTopics.toArray()) {
                byte[] topic = Hex.decode(((String) t));
                topics.add(new DataWord(topic));
            }

            LogInfo li = new LogInfo(address, topics, data);
            logs.add(li);
        }
    }


    public Iterator<LogInfo> getIterator() {
        return logs.iterator();
    }


    public List<String> compareToReal(List<LogInfo> logResult) {
        List<String> results = new ArrayList<>();
        if (logsHash != null) {
            byte[][] logInfoListE = new byte[logResult.size()][];
            for (int i = 0; i < logResult.size(); i++) {
                logInfoListE[i] = logResult.get(i).getEncoded();
            }
            byte[] logInfoListRLP = RLP.encodeList(logInfoListE);
            byte[] resHash = HashUtil.sha3(logInfoListRLP);
            if (!FastByteComparisons.equal(logsHash, resHash)) {
                results.add("Logs hash doesn't match expected: " + Hex.toHexString(resHash) + " != " + Hex.toHexString(logsHash));
            }
        } else {
            Iterator<LogInfo> postLogs = getIterator();
            int i = 0;
            while (postLogs.hasNext()) {

                LogInfo expectedLogInfo = postLogs.next();

                LogInfo foundLogInfo = null;
                if (logResult.size() > i)
                    foundLogInfo = logResult.get(i);

                if (foundLogInfo == null) {
                    String output =
                            String.format("Expected log [ %s ]", expectedLogInfo.toString());
                    results.add(output);
                } else {
                    if (!Arrays.equals(expectedLogInfo.getAddress(), foundLogInfo.getAddress())) {
                        String output =
                                String.format("Expected address [ %s ], found [ %s ]", Hex.toHexString(expectedLogInfo.getAddress()), Hex.toHexString(foundLogInfo.getAddress()));
                        results.add(output);
                    }

                    if (!Arrays.equals(expectedLogInfo.getData(), foundLogInfo.getData())) {
                        String output =
                                String.format("Expected data [ %s ], found [ %s ]", Hex.toHexString(expectedLogInfo.getData()), Hex.toHexString(foundLogInfo.getData()));
                        results.add(output);
                    }

                    if (!expectedLogInfo.getBloom().equals(foundLogInfo.getBloom())) {
                        String output =
                                String.format("Expected bloom [ %s ], found [ %s ]",
                                        Hex.toHexString(expectedLogInfo.getBloom().getData()),
                                        Hex.toHexString(foundLogInfo.getBloom().getData()));
                        results.add(output);
                    }

                    if (expectedLogInfo.getTopics().size() != foundLogInfo.getTopics().size()) {
                        String output =
                                String.format("Expected number of topics [ %d ], found [ %d ]",
                                        expectedLogInfo.getTopics().size(), foundLogInfo.getTopics().size());
                        results.add(output);
                    } else {
                        int j = 0;
                        for (DataWord topic : expectedLogInfo.getTopics()) {
                            byte[] foundTopic = foundLogInfo.getTopics().get(j).getData();

                            if (!Arrays.equals(topic.getData(), foundTopic)) {
                                String output =
                                        String.format("Expected topic [ %s ], found [ %s ]", Hex.toHexString(topic.getData()), Hex.toHexString(foundTopic));
                                results.add(output);
                            }

                            ++j;
                        }
                    }
                }

                ++i;
            }
        }
        return results;
    }
}

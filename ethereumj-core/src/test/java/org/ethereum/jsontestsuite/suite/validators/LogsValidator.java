package org.ethereum.jsontestsuite.suite.validators;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class LogsValidator {

    public static List<String> valid(List<LogInfo> origLogs, List<LogInfo> postLogs) {

        List<String> results = new ArrayList<>();

        int i = 0;
        for (LogInfo postLog : postLogs) {

            if (origLogs == null || origLogs.size() - 1 < i){
                String formattedString = String.format("Log: %s: was expected but doesn't exist: address: %s",
                        i, Hex.toHexString(postLog.getAddress()));
                results.add(formattedString);

                continue;
            }

            LogInfo realLog = origLogs.get(i);

            String postAddress = Hex.toHexString(postLog.getAddress());
            String realAddress = Hex.toHexString(realLog.getAddress());

            if (!postAddress.equals(realAddress)) {

                String formattedString = String.format("Log: %s: has unexpected address, expected address: %s found address: %s",
                        i, postAddress, realAddress);
                results.add(formattedString);
            }

            String postData = Hex.toHexString(postLog.getData());
            String realData = Hex.toHexString(realLog.getData());

            if (!postData.equals(realData)) {

                String formattedString = String.format("Log: %s: has unexpected data, expected data: %s found data: %s",
                        i, postData, realData);
                results.add(formattedString);
            }

            String postBloom = Hex.toHexString(postLog.getBloom().getData());
            String realBloom = Hex.toHexString(realLog.getBloom().getData());

            if (!postData.equals(realData)) {

                String formattedString = String.format("Log: %s: has unexpected bloom, expected bloom: %s found bloom: %s",
                        i, postBloom, realBloom);
                results.add(formattedString);
            }

            List<DataWord> postTopics = postLog.getTopics();
            List<DataWord> realTopics = realLog.getTopics();

            int j = 0;
            for (DataWord postTopic : postTopics) {

                DataWord realTopic = realTopics.get(j);

                if (!postTopic.equals(realTopic)) {

                    String formattedString = String.format("Log: %s: has unexpected topic: %s, expected topic: %s found topic: %s",
                            i, j, postTopic, realTopic);
                    results.add(formattedString);
                }
                ++j;
            }

            ++i;
        }

        return results;
    }

}

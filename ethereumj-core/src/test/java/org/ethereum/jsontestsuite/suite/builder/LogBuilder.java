package org.ethereum.jsontestsuite.suite.builder;

import org.ethereum.jsontestsuite.suite.model.LogTck;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.jsontestsuite.suite.Utils.parseData;

public class LogBuilder {

    public static LogInfo build(LogTck logTck){

        byte[] address = parseData(logTck.getAddress());
        byte[] data = parseData(logTck.getData());

        List<DataWord> topics = new ArrayList<>();
        for (String topicTck : logTck.getTopics())
            topics.add(new DataWord(parseData(topicTck)));

        return new LogInfo(address, topics, data);
    }

    public static List<LogInfo> build(List<LogTck> logs){

        List<LogInfo> outLogs = new ArrayList<>();

        for (LogTck log : logs)
            outLogs.add(build(log));

        return outLogs;
    }
}

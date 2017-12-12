package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.jsontestsuite.GitHubJSONTestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mikhail Kalinin
 * @since 09.08.2017
 */
public class StateTestData {

    Map<String, StateTestDataEntry> testData;

    public StateTestData(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, StateTestDataEntry.class);

        testData = new ObjectMapper().readValue(json, type);
    }

    public List<StateTestCase> getTestCases(GitHubJSONTestSuite.Network network) {

        List<StateTestCase> cases = new ArrayList<>();

        for (Map.Entry<String, StateTestDataEntry> e : testData.entrySet()) {
            List<StateTestCase> testCases = e.getValue().getTestCases(network);
            for (int i = 0; i < testCases.size(); i++) {
                StateTestCase testCase = testCases.get(i);
                testCase.setName(String.format("%s_%s%s", e.getKey(), network.name(),
                        testCases.size() > 1 ? "_" + String.valueOf(i + 1) : ""));
            }
            cases.addAll(testCases);
        }

        return cases;
    }

}

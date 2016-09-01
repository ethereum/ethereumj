package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2015
 */
public class EthashTestSuite {

    List<EthashTestCase> testCases = new ArrayList<>();

    public EthashTestSuite(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, EthashTestCase.class);

        Map<String, EthashTestCase> caseMap = new ObjectMapper().readValue(json, type);

        for (Map.Entry<String, EthashTestCase> e : caseMap.entrySet()) {
            e.getValue().setName(e.getKey());
            testCases.add(e.getValue());
        }
    }

    public List<EthashTestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "EthashTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

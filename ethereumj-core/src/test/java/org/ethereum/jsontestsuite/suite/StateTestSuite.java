package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StateTestSuite {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");

    Map<String, StateTestCase> testCases = new HashMap<>();

    public StateTestSuite(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, StateTestCase.class);

        testCases = new ObjectMapper().readValue(json, type);
    }

    public Map<String, StateTestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "StateTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

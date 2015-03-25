package org.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StateTestSuite2 {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");

    Map<String, StateTestCase2> testCases = new HashMap<>();

    public StateTestSuite2(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, StateTestCase2.class);

        testCases = new ObjectMapper().readValue(json, type);
    }

    public Map<String, StateTestCase2> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "StateTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

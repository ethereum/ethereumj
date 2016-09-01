package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockTestSuite {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");

    Map<String, BlockTestCase> testCases = new HashMap<>();

    public BlockTestSuite(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, BlockTestCase.class);

        testCases = new ObjectMapper().readValue(json, type);
    }

    public Map<String, BlockTestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "BlockTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

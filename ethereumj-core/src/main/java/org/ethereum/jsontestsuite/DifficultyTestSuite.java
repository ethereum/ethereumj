package org.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyTestSuite {

    Map<String, DifficultyTestCase> testCases = new HashMap<>();

    public DifficultyTestSuite(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, DifficultyTestCase.class);

        testCases = new ObjectMapper().readValue(json, type);
    }

    public Map<String, DifficultyTestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "DifficultyTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

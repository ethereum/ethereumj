package org.ethereum.jsontestsuite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyTestSuite {

    List<DifficultyTestCase> testCases = new ArrayList<>();

    public DifficultyTestSuite(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, DifficultyTestCase.class);

        Map<String, DifficultyTestCase> caseMap = new ObjectMapper().readValue(json, type);

        for (Map.Entry<String, DifficultyTestCase> e : caseMap.entrySet()) {
            e.getValue().setName(e.getKey());
            testCases.add(e.getValue());
        }

        Collections.sort(testCases, new Comparator<DifficultyTestCase>() {
            @Override
            public int compare(DifficultyTestCase t1, DifficultyTestCase t2) {
                return t1.getName().compareTo(t2.getName());
            }
        });
    }

    public List<DifficultyTestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "DifficultyTestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

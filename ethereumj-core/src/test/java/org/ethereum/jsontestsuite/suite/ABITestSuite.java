package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2017
 */
public class ABITestSuite {

    List<ABITestCase> testCases = new ArrayList<>();

    public ABITestSuite(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().
                constructMapType(HashMap.class, String.class, ABITestCase.class);

        Map<String, ABITestCase> caseMap = new ObjectMapper().readValue(json, type);

        for (Map.Entry<String, ABITestCase> e : caseMap.entrySet()) {
            e.getValue().setName(e.getKey());
            testCases.add(e.getValue());
        }

        Collections.sort(testCases, new Comparator<ABITestCase>() {
            @Override
            public int compare(ABITestCase t1, ABITestCase t2) {
                return t1.getName().compareTo(t2.getName());
            }
        });
    }

    public List<ABITestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "ABITestSuite{" +
                "testCases=" + testCases +
                '}';
    }
}

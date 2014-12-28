package org.ethereum.jsontestsuite;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 10.07.2014
 */
public class StateTestSuite {

    private Logger logger = LoggerFactory.getLogger("TCK-Test");
    Map<String, StateTestCase> testCases = new HashMap<>();

    public StateTestSuite(JSONObject testCaseJSONObj) throws ParseException {

        for (Object key : testCaseJSONObj.keySet()) {

            Object testCaseJSON = testCaseJSONObj.get(key);

            StateTestCase testCase = new StateTestCase(key.toString(), (JSONObject) testCaseJSON);

            testCases.put(key.toString(), testCase);
        }
    }

    public StateTestCase getTestCase(String name) {

        StateTestCase testCase = testCases.get(name);
        if (testCase == null) throw new NullPointerException("Test cases doesn't exist: " + name);

        return testCase;
    }

    public Collection<StateTestCase> getAllTests() {
        return testCases.values();
    }


}

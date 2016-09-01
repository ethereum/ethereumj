package org.ethereum.jsontestsuite.suite;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 10.07.2014
 */
public class TestSuite {

    List<TestCase> testList = new ArrayList<>();

    public TestSuite(JSONObject testCaseJSONObj) throws ParseException {

        for (Object key : testCaseJSONObj.keySet()) {

            Object testCaseJSON = testCaseJSONObj.get(key);
            TestCase testCase = new TestCase(key.toString(), (JSONObject) testCaseJSON);
            testList.add(testCase);
        }
    }

    public List<TestCase> getAllTests(){
        return testList;
    }

    public Iterator<TestCase> iterator() {
        return testList.iterator();
    }
}

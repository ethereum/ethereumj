package org.ethereum.jsontestsuite;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 10/07/2014 09:46
 */

public class TestSuite {

    List<TestCase> testList = new ArrayList<>();

    public TestSuite(JSONObject testCaseJSONObj) throws ParseException {

        for (Object key: testCaseJSONObj.keySet()){

            Object testCaseJSON = testCaseJSONObj.get(key);
            TestCase testCase = new TestCase(key.toString(), (JSONObject) testCaseJSON);
            testList.add(testCase);
        }
    }

    public Iterator<TestCase> iterator(){
        return testList.iterator();
    }
}

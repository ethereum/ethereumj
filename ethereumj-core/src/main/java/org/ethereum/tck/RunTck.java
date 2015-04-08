package org.ethereum.tck;

import org.ethereum.jsontestsuite.*;
import org.ethereum.jsontestsuite.runners.StateTestRunner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunTck {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");


    public static void main(String[] args) throws ParseException, IOException {

        if (args.length > 0){

            if (args[0].equals("filerun")) {
                logger.info("TCK Running, file: " + args[1]);
                runTest(args[1]);
            } else if ((args[0].equals("content"))) {
                logger.debug("TCK Running, content: ");
                runContentTest(args[1].replaceAll("'", "\""));
            }

        } else {
            logger.info("No test case specified");
        }
    }

    public static void runContentTest(String content) throws ParseException, IOException {

        Map<String, Boolean> summary = new HashMap<>();

        JSONParser parser = new JSONParser();
        JSONObject testSuiteObj = (JSONObject) parser.parse(content);

        StateTestSuite stateTestSuite = new StateTestSuite(testSuiteObj.toJSONString());
        Map<String, StateTestCase> testCases = stateTestSuite.getTestCases();

        for (String testName : testCases.keySet()) {

            logger.info(" Test case: {}", testName);

            StateTestCase stateTestCase = testCases.get(testName);
            List<String> result = StateTestRunner.run(stateTestCase);

            if (!result.isEmpty())
                summary.put(testName, false);
            else
                summary.put(testName, true);
        }

        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "PASS" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" - Total: Pass: {}, Failed: {} - ", pass, fails);

        if (fails > 0)
            System.exit(1);
        else
            System.exit(0);

    }



    public static void runTest(String name) throws ParseException, IOException {

        String testCaseJson = JSONReader.getFromLocal(name);
        runContentTest(testCaseJson);
    }
}

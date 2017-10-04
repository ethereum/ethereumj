/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.jsontestsuite.suite;

import org.ethereum.jsontestsuite.GitHubJSONTestSuite;
import org.ethereum.jsontestsuite.suite.runners.StateTestRunner;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.ethereum.jsontestsuite.suite.JSONReader.listJsonBlobsForTreeSha;
import static org.ethereum.jsontestsuite.suite.JSONReader.loadJSONFromCommit;
import static org.ethereum.jsontestsuite.suite.JSONReader.loadJSONsFromCommit;

/**
 * @author Mikhail Kalinin
 * @since 11.08.2017
 */

public class GeneralStateTestSuite {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    private static final String STATE_TEST_ROOT = "GeneralStateTests/";

    String commitSHA;
    List<String> files;
    GitHubJSONTestSuite.Network[] networks;

    public GeneralStateTestSuite(String treeSHA, String commitSHA, GitHubJSONTestSuite.Network[] networks) throws IOException {
        files = listJsonBlobsForTreeSha(treeSHA, STATE_TEST_ROOT);
        this.commitSHA = commitSHA;
        this.networks = networks;
    }

    private static void run(List<String> checkFiles,
                            String commitSHA,
                            GitHubJSONTestSuite.Network[] networks) throws IOException {

        if (checkFiles.isEmpty()) return;

        List<StateTestData> suites = new ArrayList<>();
        List<String> filenames = new ArrayList<>();
        for (String file : checkFiles) {
            filenames.add(STATE_TEST_ROOT + file);
        }

        List<String> jsons = loadJSONsFromCommit(filenames, commitSHA);
        for (String json : jsons) {
            suites.add(new StateTestData(json));
        }

        List<StateTestCase> testCases = new ArrayList<>();
        for (GitHubJSONTestSuite.Network network : networks) {
            for (StateTestData suite : suites) {
                testCases.addAll(suite.getTestCases(network));
            }
        }

        Map<String, Boolean> summary = new HashMap<>();

        for (StateTestCase testCase : testCases) {

            String output = String.format("*  running: %s  *", testCase.getName());
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);

            List<String> result = StateTestRunner.run(testCase);
            if (!result.isEmpty())
                summary.put(testCase.getName(), false);
            else
                summary.put(testCase.getName(), true);
        }

        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "OK" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" - Total: Pass: {}, Failed: {} - ", pass, fails);

        Assert.assertTrue(fails == 0);
    }

    public void runAll(String testCaseRoot, Set<String> excludedCases, GitHubJSONTestSuite.Network[] networks) throws IOException {

        List<String> testCaseFiles = new ArrayList<>();
        for (String file : files) {
            if (file.startsWith(testCaseRoot + "/")) testCaseFiles.add(file);
        }

        Set<String> toExclude = new HashSet<>();
        for (String file : testCaseFiles) {
            String fileName = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf(".json"));
            if (excludedCases.contains(fileName)) {
                logger.info(" [X] " + file);
                toExclude.add(file);
            } else {
                logger.info("     " + file);
            }
        }

        testCaseFiles.removeAll(toExclude);

        run(testCaseFiles, commitSHA, networks);
    }

    public void runAll(String testCaseRoot) throws IOException {
        runAll(testCaseRoot, Collections.<String>emptySet(), networks);
    }

    public void runAll(String testCaseRoot, Set<String> excludedCases) throws IOException {
        runAll(testCaseRoot, excludedCases, networks);
    }

    public void runAll(String testCaseRoot, GitHubJSONTestSuite.Network network) throws IOException {
        runAll(testCaseRoot, Collections.<String>emptySet(), new GitHubJSONTestSuite.Network[]{network});
    }

    public static void runSingle(String testFile, String commitSHA,
                                 GitHubJSONTestSuite.Network network) throws IOException {
        logger.info("     " + testFile);
        run(Collections.singletonList(testFile), commitSHA, new GitHubJSONTestSuite.Network[] { network });
    }
}

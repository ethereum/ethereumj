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

import static org.ethereum.jsontestsuite.GitHubJSONTestSuite.runGitHubJsonTransactionTest;
import static org.ethereum.jsontestsuite.suite.JSONReader.listJsonBlobsForTreeSha;
import static org.ethereum.jsontestsuite.suite.JSONReader.loadJSONFromCommit;
import static org.ethereum.jsontestsuite.suite.JSONReader.loadJSONsFromCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikhail Kalinin
 * @since 11.09.2017
 */
public class TxTestSuite {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    private static final String TEST_ROOT = "TransactionTests/";

    String commitSHA;
    List<String> files;

    public TxTestSuite(String treeSHA, String commitSHA) throws IOException {
        files = listJsonBlobsForTreeSha(treeSHA, TEST_ROOT);
        this.commitSHA = commitSHA;
    }

    public void runAll(String testCaseRoot) throws ParseException, IOException {
        run(testCaseRoot, Collections.<String>emptySet());
    }

    public void run(String testCaseRoot, Set<String> excluded) throws ParseException, IOException {

        List<String> testCaseFiles = new ArrayList<>();
        for (String file : files) {
            if (file.startsWith(testCaseRoot + "/")) testCaseFiles.add(file);
        }

        Set<String> toExclude = new HashSet<>();
        for (String file : testCaseFiles) {
            String fileName = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf(".json"));
            if (excluded.contains(fileName)) {
                logger.info(" [X] " + file);
                toExclude.add(file);
            } else {
                logger.info("     " + file);
            }
        }

        testCaseFiles.removeAll(toExclude);

        if (testCaseFiles.isEmpty()) return;

        List<String> filenames = new ArrayList<>();
        for (String file : testCaseFiles) {
            filenames.add(TEST_ROOT + file);
        }

        List<String> jsons = loadJSONsFromCommit(filenames, commitSHA);
        for (String json : jsons) {
            runGitHubJsonTransactionTest(json);
        }
    }

    public static void runSingle(String commitSHA, String file) throws ParseException, IOException {
        logger.info("     " + file);
        String json = loadJSONFromCommit(TEST_ROOT + file, commitSHA);
        runGitHubJsonTransactionTest(json);
    }
}

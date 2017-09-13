package org.ethereum.jsontestsuite.suite;

import org.ethereum.jsontestsuite.GitHubJSONTestSuite;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.ethereum.jsontestsuite.GitHubJSONTestSuite.runGitHubJsonVMTest;
import static org.ethereum.jsontestsuite.suite.JSONReader.listJsonBlobsForTreeSha;
import static org.ethereum.jsontestsuite.suite.JSONReader.loadJSONFromCommit;
import static org.ethereum.jsontestsuite.suite.JSONReader.loadJSONsFromCommit;

/**
 * @author Mikhail Kalinin
 * @since 08.09.2017
 */
public class VMTestSuite {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    private static final String TEST_ROOT = "VMTests/";

    String commitSHA;
    List<String> files;

    public VMTestSuite(String treeSHA, String commitSHA) {
        files = listJsonBlobsForTreeSha(treeSHA);
        this.commitSHA = commitSHA;
    }

    public void runAll(String testCaseRoot) throws ParseException {
        run(testCaseRoot, Collections.<String>emptySet());
    }

    public void run(String testCaseRoot, Set<String> excluded) throws ParseException {

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

        List<String> jsons = loadJSONsFromCommit(filenames, commitSHA, 64);
        for (String json : jsons) {
            runGitHubJsonVMTest(json);
        }
    }

    public static void runSingle(String commitSHA, String file) throws ParseException {
        logger.info("     " + file);
        String json = loadJSONFromCommit(TEST_ROOT + file, commitSHA);
        runGitHubJsonVMTest(json);
    }
}

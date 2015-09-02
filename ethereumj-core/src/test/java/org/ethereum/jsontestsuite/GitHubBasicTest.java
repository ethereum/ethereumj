package org.ethereum.jsontestsuite;

import org.ethereum.validator.DifficultyRule;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBasicTest {

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");

    @Test
    public void runDifficultyTest() throws IOException, ParseException {

        String shacommit = "b814f9fb484889bda407f2b9349983f211b77753";
        String json = JSONReader.loadJSONFromCommit("BasicTests/difficulty.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        DifficultyRule rule = new DifficultyRule();

        boolean passed = true;

        for (Map.Entry<String, DifficultyTestCase> e : testSuite.getTestCases().entrySet()) {
            DifficultyTestCase testCase = e.getValue();

            long timestamp = Long.valueOf(testCase.getCurrentTimestamp());
            long parentTimestamp = Long.valueOf(testCase.getParentTimestamp());
            BigInteger difficulty = new BigInteger(testCase.getCurrentDifficulty());
            BigInteger parentDifficulty = new BigInteger(testCase.getParentDifficulty());

            boolean valid = rule.validate(timestamp, parentTimestamp, difficulty, parentDifficulty);
            logger.info("\nTest {}: {}", e.getKey(), valid ? "passed" : "failed");

            if (!valid) {
                passed = false;
            }
        }

        assertTrue(passed);
    }
}

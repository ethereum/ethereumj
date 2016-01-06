package org.ethereum.jsontestsuite;

import org.ethereum.config.Constants;
import org.ethereum.core.BlockHeader;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBasicTest {
    private static final long HOMESTEAD_BLOCK_SAVE = Constants.HOMESTEAD_FORK_BLKNUM;

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");
    public String shacommit = "0895e096ca9de6ba745bad238cb579964bd90cea";

    @Before
    public void setup() {

        // if not set explicitly
        // this test fails being run by Gradle
        CONFIG.setGenesisInfo("frontier.json");
    }

    @After
    public void recover() {
        Constants.HOMESTEAD_FORK_BLKNUM = HOMESTEAD_BLOCK_SAVE;
    }

    @Test
    public void runDifficultyTest() throws IOException, ParseException {

        Constants.HOMESTEAD_FORK_BLKNUM = Long.MAX_VALUE;

        String json = JSONReader.loadJSONFromCommit("BasicTests/difficulty.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty(parent));
        }
    }

    @Test
    public void runDifficultyFrontierTest() throws IOException, ParseException {

        Constants.HOMESTEAD_FORK_BLKNUM = Long.MAX_VALUE;

        String json = JSONReader.loadJSONFromCommit("BasicTests/difficultyFrontier.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty(parent));
        }
    }

    @Test
    public void runDifficultyHomesteadTest() throws IOException, ParseException {

        Constants.HOMESTEAD_FORK_BLKNUM = 0;

        String json = JSONReader.loadJSONFromCommit("BasicTests/difficultyHomestead.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty(parent));
        }
    }
}

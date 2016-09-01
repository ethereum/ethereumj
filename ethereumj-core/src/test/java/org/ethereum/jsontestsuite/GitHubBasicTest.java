package org.ethereum.jsontestsuite;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.core.BlockHeader;
import org.ethereum.jsontestsuite.suite.DifficultyTestCase;
import org.ethereum.jsontestsuite.suite.DifficultyTestSuite;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBasicTest {

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");
    public String shacommit = "92bb72cccf4b5a2d29d74248fdddfe8b43baddda";

    @After
    public void recover() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Test
    public void runDifficultyTest() throws IOException, ParseException {

        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);

        String json = JSONReader.loadJSONFromCommit("BasicTests/difficulty.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty
                    (SystemProperties.getDefault().getBlockchainConfig(), parent));
        }
    }

    @Test
    public void runDifficultyFrontierTest() throws IOException, ParseException {

        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);

        String json = JSONReader.loadJSONFromCommit("BasicTests/difficultyFrontier.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty(
                    SystemProperties.getDefault().getBlockchainConfig(), parent));
        }
    }

    @Test
    public void runDifficultyHomesteadTest() throws IOException, ParseException {

        SystemProperties.getDefault().setBlockchainConfig(new HomesteadConfig());

        String json = JSONReader.loadJSONFromCommit("BasicTests/difficultyHomestead.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty(
                    SystemProperties.getDefault().getBlockchainConfig(), parent));
        }
    }
}

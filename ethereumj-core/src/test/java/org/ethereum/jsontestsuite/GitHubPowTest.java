package org.ethereum.jsontestsuite;

import org.ethereum.core.BlockHeader;
import org.ethereum.jsontestsuite.suite.EthashTestCase;
import org.ethereum.jsontestsuite.suite.EthashTestSuite;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2015
 */
public class GitHubPowTest {

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");
    public String shacommit = "92bb72cccf4b5a2d29d74248fdddfe8b43baddda";

    @Test
    public void runEthashTest() throws IOException {

        String json = JSONReader.loadJSONFromCommit("PoWTests/ethash_tests.json", shacommit);

        EthashTestSuite testSuite = new EthashTestSuite(json);

        for (EthashTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader header = testCase.getBlockHeader();

            assertArrayEquals(testCase.getResultBytes(), header.calcPowValue());
        }

    }
}

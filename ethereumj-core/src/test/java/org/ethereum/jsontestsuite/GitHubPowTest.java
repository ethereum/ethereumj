package org.ethereum.jsontestsuite;

import org.ethereum.core.BlockHeader;
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

    @Test
    public void runEthashTest() throws IOException {

        String shacommit = "c6d96293710a37489fa3b074a9fc228e0393f152";
        String json = JSONReader.loadJSONFromCommit("PoWTests/ethash_tests.json", shacommit);

        EthashTestSuite testSuite = new EthashTestSuite(json);

        for (EthashTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader header = testCase.getBlockHeader();

            assertArrayEquals(testCase.getResultBytes(), header.calcPowValue());
        }

    }
}

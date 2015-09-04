package org.ethereum.jsontestsuite;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.json.simple.parser.ParseException;
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

    private static final Logger logger = LoggerFactory.getLogger("TCK-Test");

    @Before
    public void setup() {

        // if not set explicitly
        // this test fails being run by Gradle
        CONFIG.setGenesisInfo("frontier.json");
    }

    @Test
    public void runDifficultyTest() throws IOException, ParseException {

        String shacommit = "b814f9fb484889bda407f2b9349983f211b77753";
        String json = JSONReader.loadJSONFromCommit("BasicTests/difficulty.json", shacommit);

        DifficultyTestSuite testSuite = new DifficultyTestSuite(json);

        for (DifficultyTestCase testCase : testSuite.getTestCases()) {

            logger.info("Running {}\n", testCase.getName());

            BlockHeader current = testCase.getCurrent();
            BlockHeader parent = testCase.getParent();

            assertEquals(testCase.getExpectedDifficulty(), current.calcDifficulty(parent));
        }
    }
}

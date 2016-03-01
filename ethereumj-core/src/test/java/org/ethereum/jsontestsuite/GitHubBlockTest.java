package org.ethereum.jsontestsuite;

import org.ethereum.config.Constants;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import static org.ethereum.config.SystemProperties.CONFIG;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "f28ac81493281feec0b17290565cf74042893677";
    private static final long HOMESTEAD_TEST_BLOCK = 0;
    private static final long HOMESTEAD_BLOCK = Constants.HOMESTEAD_FORK_BLKNUM;

    @Ignore // test for conveniently running a single test
    @Test
    public void runSingleTest() throws ParseException, IOException {
        CONFIG.setGenesisInfo("frontier.json");
        Constants.HOMESTEAD_FORK_BLKNUM = HOMESTEAD_TEST_BLOCK;

        String json = JSONReader.loadJSONFromCommit("BlockchainTests/Homestead/bcTotalDifficultyTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonSingleBlockTest(json, "sideChainWithNewMaxDifficultyStartingFromBlock3AfterBlock4");
    }

    private void runFrontier(String name) throws IOException, ParseException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/" + name + ".json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
    }

    private void runHomestead(String name) throws IOException, ParseException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/Homestead/" + name + ".json", shacommit);
        Constants.HOMESTEAD_FORK_BLKNUM = HOMESTEAD_TEST_BLOCK;
        try {
            GitHubJSONTestSuite.runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
        } finally {
            Constants.HOMESTEAD_FORK_BLKNUM = HOMESTEAD_BLOCK;
        }
    }

    private void run(String name, boolean frontier, boolean homestead) throws IOException, ParseException {
        if (frontier) runFrontier(name);
        if (homestead) runHomestead(name);
    }

    @Test
    public void runBCInvalidHeaderTest() throws ParseException, IOException {
        run("bcInvalidHeaderTest", true, true);
    }


    @Test
    public void runBCInvalidRLPTest() throws ParseException, IOException {
        run("bcInvalidRLPTest", true, false);
    }

    @Test
    public void runBCRPCAPITest() throws ParseException, IOException {
        run("bcRPC_API_Test", true, true);
    }


    @Test
    public void runBCUncleHeaderValidityTest() throws ParseException, IOException {
        run("bcUncleHeaderValiditiy", true, true);
    }

    @Test
     public void runBCUncleTest() throws ParseException, IOException {
        run("bcUncleTest", true, true);
    }

    @Test
    public void runBCValidBlockTest() throws ParseException, IOException {
        CONFIG.setGenesisInfo("frontier.json");
        run("bcValidBlockTest", true, true);
    }

    @Test
    public void runBCBlockGasLimitTest() throws ParseException, IOException {
        run("bcBlockGasLimitTest", true, true);
    }

    @Test
    public void runBCForkBlockTest() throws ParseException, IOException {
        run("bcForkBlockTest", true, false);
    }

    @Test
    public void runBCForkUncleTest() throws ParseException, IOException {
        run("bcForkUncle", true, false);
    }

    @Test
    public void runBCForkStressTest() throws ParseException, IOException {
        run("bcForkStressTest", true, true);
    }

    @Test
    public void runBCStateTest() throws ParseException, IOException {
        run("bcStateTest", true, true);
    }

    @Test
    public void runBCGasPricerTest() throws ParseException, IOException {
        run("bcGasPricerTest", true, true);
    }

    @Test
    public void runBCTotalDifficultyTest() throws ParseException, IOException {
        run("bcTotalDifficultyTest", false, true);
    }

    @Test
    public void runBCWalletTest() throws Exception, IOException {
        run("bcWalletTest", true, true);
    }

    @Test
    public void runBCMultiChainTest() throws ParseException, IOException {
        run("bcMultiChainTest", true, true);
    }
}

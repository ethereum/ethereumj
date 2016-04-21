package org.ethereum.jsontestsuite;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.net.MainNetConfig;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Collections;



@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "0895e096ca9de6ba745bad238cb579964bd90cea";

    @Ignore // test for conveniently running a single test
    @Test
    public void runSingleTest() throws ParseException, IOException {
        SystemProperties config = SystemProperties.getDefault();
        config.setGenesisInfo("frontier.json");
        config.setBlockchainConfig(new HomesteadConfig());

        String json = JSONReader.loadJSONFromCommit("BlockchainTests/Homestead/bcTotalDifficultyTest.json", shacommit);
        new GitHubJSONTestSuite(config).runGitHubJsonSingleBlockTest(json, "sideChainWithNewMaxDifficultyStartingFromBlock3AfterBlock4");
    }

    private void runFrontier(String name) throws IOException, ParseException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/" + name + ".json", shacommit);
        new GitHubJSONTestSuite(SystemProperties.getDefault()).runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
    }

    private void runHomestead(String name) throws IOException, ParseException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/Homestead/" + name + ".json", shacommit);
        SystemProperties config = SystemProperties.getDefault();
        config.setBlockchainConfig(new HomesteadConfig());
        new GitHubJSONTestSuite(config).runGitHubJsonBlockTest(json, Collections.EMPTY_SET);
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
//        CONFIG.setGenesisInfo("frontier.json");
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

package org.ethereum.jsontestsuite;

import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "57a5bc6cee88cb09577475fbd8f15c87e6e86fd1";

    @Ignore
    @Test
    public void runSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonSingleBlockTest(json, "oneUncleGeneration7");
    }


    @Ignore
    @Test
    public void runBCInvalidHeaderTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcInvalidHeaderTest.json", shacommit);
        //TODO fix via blockchain rollbacks
        excluded.add("wrongGasUsed");
        excluded.add("wrongStateRoot");
        //TODO need to make sure the checks match the cpp
        excluded.add("wrongParentHash");
        excluded.add("wrongTimestamp");
        //TODO need to add some way to roll back repository from BlockChain.add()
        excluded.add("wrongReceiptTrie");
        excluded.add("log1_wrongBloom");

        GitHubJSONTestSuite.runGitHubJsonBlockTest(json,excluded);
    }

    @Ignore
    @Test
    public void runBCInvalidRLPTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcInvalidRLPTest.json", shacommit);
        excluded.add("BLOCK_stateRoot_GivenAsList");
        excluded.add("BLOCK_difficulty_GivenAsList");
        excluded.add("BLOCK_mixHash_TooShort");
        excluded.add("BLOCK__RandomByteAtRLP_8");
        excluded.add("BLOCK__RandomByteAtRLP_9");
        excluded.add("BLOCK__RandomByteAtRLP_7");
        excluded.add("BLOCK__RandomByteAtRLP_6");
        excluded.add("BLOCK__RandomByteAtRLP_5");
        excluded.add("BLOCK__RandomByteAtRLP_4");
        excluded.add("BLOCK_stateRoot_TooShort");
        excluded.add("BLOCK_gasUsed_TooLarge");
        excluded.add("BLOCK_stateRoot_TooLarge");
        excluded.add("BLOCK_receiptTrie_Prefixed0000");
        excluded.add("BLOCK_transactionsTrie_TooLarge");
        excluded.add("TRANSCT_gasLimit_Prefixed0000");
        excluded.add("TRANSCT_gasLimit_GivenAsList");
        excluded.add("TRANSCT_svalue_TooLarge");
        excluded.add("TRANSCT_svalue_TooShort");
        excluded.add("TRANSCT_svalue_GivenAsList");
        excluded.add("TRANSCT__RandomByteAtTheEnd");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

//    @Ignore
    @Test
    public void runBCRPCAPITest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcRPC_API_Test.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCUncleHeaderValidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcUncleHeaderValiditiy.json", shacommit);
        //TODO need to make sure these are not passing on accident
        excluded.add("timestampTooLow");
        excluded.add("timestampTooHigh");
        excluded.add("wrongParentHash");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

//    @Ignore
    @Test
     public void runBCUncleTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    //    @Ignore
    @Test
    public void runBCValidBlockTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcValidBlockTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCBlockGasLimitTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcBlockGasLimitTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

//    @Ignore
    @Test
    public void runBCBruncleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcBruncleTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

//    @Ignore
    @Test
    public void runBCForkBlockTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcForkBlockTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCGasPricerTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcGasPricerTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCTotalDifficultyTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcTotalDifficultyTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        excluded.add("lotsOfBranchesOverrideAtTheEnd");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

//    @Ignore
    @Test
    public void runBCWalletTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockchainTests/bcWalletTest.json", shacommit);
        Set<String> excluded = new HashSet<>();
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }


}

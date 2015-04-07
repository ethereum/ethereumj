package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;
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
    public String shacommit = "dbc3241e70dd3388cb87b1509f686893125d5f0b";

    @Ignore
    @Test
    public void runSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonSingleBlockTest(json, "oneUncleGeneration7");
    }


    @Ignore
    @Test
    public void runBCBlockChainTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcInvalidHeaderTest.json", shacommit);
        //TODO figure out if these need to have POSTs or not, cpp doesnt check
        excluded.add("wrongNumber");
        excluded.add("wrongDifficulty");
        excluded.add("wrongTimestamp");
        excluded.add("wrongGasLimit");
        excluded.add("wrongParentHash");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json,excluded);
        //GitHubJSONTestSuite.runGitHubJsonSingleBlockTest(json, "wrongDifficulty");
    }

    @Ignore
    @Test
    public void runBCInvalidRLPTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcInvalidRLPTest.json", shacommit);
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

    @Ignore
    @Test
    public void runBCJSAPITest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcJS_API_Test.json", shacommit);
        excluded.add("JS_API_Tests");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCUncleHeaderValidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleHeaderValiditiy.json", shacommit);
        excluded.add("timestampTooLow");
        excluded.add("timestampTooHigh");
        excluded.add("wrongParentHash");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
     public void runBCUncleTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        excluded.add("uncleWithSameBlockNumber");
        excluded.add("oneUncleGeneration6");
        excluded.add("oneUncleGeneration7");
        excluded.add("InChainUncle");
        excluded.add("threeUncle");
        excluded.add("twoEqualUncle");
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCValidBlockTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcValidBlockTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }



}

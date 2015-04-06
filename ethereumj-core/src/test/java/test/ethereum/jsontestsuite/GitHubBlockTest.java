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
    public String shacommit = "65112b9c04d4f97136b2fd238e6688610a98aabf";

    @Ignore
    @Test
    public void runSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonSingleBlockTest(json, "oneUncleGeneration7");
    }


    //@Ignore
    @Test
    public void runBCBlockChainTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcInvalidHeaderTest.json", shacommit);
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
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json, excluded);
    }

    @Ignore
    @Test
    public void runBCJSAPITest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcJS_API_Test.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }


    @Ignore
    @Test
     public void runBCUncleTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcUncleTest.json", shacommit);
        excluded.add("uncleWithSameBlockNumber");
        excluded.add("uncleHeaderWithGeneration0");
        excluded.add("oneUncleGeneration2");
        excluded.add("oneUncleGeneration3");
        excluded.add("oneUncleGeneration4");
        excluded.add("oneUncleGeneration5");
        excluded.add("oneUncleGeneration6");
        excluded.add("oneUncleGeneration7");
        excluded.add("InChainUncle");
        excluded.add("oneUncle");
        excluded.add("twoUncle");
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

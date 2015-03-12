package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "b7021c7898ec1028405d70394c7ddf2445bfde6c";

    @Ignore
    @Test
    public void runSingle() throws ParseException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcBlockChainTest.json", shacommit);

        System.out.println(json);






//        GitHubJSONTestSuite.runGitHubJsonVMTest(json, "extcodecopy0AddressTooBigRight");
    }

}

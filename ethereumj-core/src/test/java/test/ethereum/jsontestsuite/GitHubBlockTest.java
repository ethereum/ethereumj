package test.ethereum.jsontestsuite;

import com.fasterxml.jackson.core.type.TypeReference;
import jdk.nashorn.internal.parser.JSONParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.ethereum.jsontestsuite.BlockTestCase;
import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubBlockTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "b7021c7898ec1028405d70394c7ddf2445bfde6c";

    @Test
    public void runBlockChainTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("BlockTests/bcBlockChainTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonBlockTest(json);
    }

}

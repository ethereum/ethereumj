package test.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubRandomTest {
    
    @Test // testing full suite
    @Ignore
    public void testRandom04FromGitHub() throws ParseException {
        String json = JSONReader.loadJSON("randomTests/201410211705.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    @Ignore
    public void testRandom06FromGitHub() throws ParseException {
        String json = JSONReader.loadJSON("randomTests/201410211708.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

}

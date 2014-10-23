package org.ethereum.jsontestsuite;

import org.json.simple.parser.ParseException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubRandomTest {
    
    @Test // testing full suite
    public void testRandom04FromGitHub() throws ParseException {
        String json = JSONReader.loadJSON("randomTests/201410211705.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }
    
    @Test // testing full suite
    public void testRandom06FromGitHub() throws ParseException {
        String json = JSONReader.loadJSON("randomTests/201410211708.json");
        GitHubJSONTestSuite.runGitHubJsonTest(json);
    }

}

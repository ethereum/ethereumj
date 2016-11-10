package org.ethereum.jsontestsuite;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTransactionTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "9028c4801fd39fbb71a9796979182549a24e81c8";

    @Before
    public void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @After
    public void recover() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @Test
    public void testTransactionTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }
}

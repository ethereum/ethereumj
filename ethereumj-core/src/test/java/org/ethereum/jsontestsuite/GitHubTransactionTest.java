package org.ethereum.jsontestsuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.DaoHFConfig;
import org.ethereum.config.blockchain.Eip150HFConfig;
import org.ethereum.config.blockchain.Eip160HFConfig;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.net.AbstractNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubTransactionTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "9028c4801fd39fbb71a9796979182549a24e81c8";

    public GitHubTransactionTest() {
        // Enable for debugging
        // LogManager.getLogger("TCK-Test").setLevel(Level.INFO);
    }

    @Before
    public void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @After
    public void recover() {
        SystemProperties.getDefault().setBlockchainConfig(new MainNetConfig());
    }

    @Test
    public void testEIP155TransactionTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        SystemProperties.getDefault().setBlockchainConfig(new AbstractNetConfig() {{
            add(0, new FrontierConfig());
            add(1_150_000, new HomesteadConfig());
            add(2_457_000, new Eip150HFConfig(new DaoHFConfig()));
            add(2_700_000, new Eip160HFConfig(new DaoHFConfig()){
                @Override
                public Integer getChainId() {
                    return null;
                }
            });
        }});
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Test
    public void testHomesteadTestsFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json1 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/tt10mbDataField.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json1, excluded);

        String json2 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json2, excluded);

        String json3 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttTransactionTestEip155VitaliksTests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json3, excluded);

        String json4 = JSONReader.loadJSONFromCommit("TransactionTests/Homestead/ttWrongRLPTransaction.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json4, excluded);
    }

    @Test
    public void testRandomTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        // pre-EIP155 wrong chain id (negative)
        String json = JSONReader.loadJSONFromCommit("TransactionTests/RandomTests/tr201506052141PYTHON.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Test
    public void testGeneralTestsFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json1 = JSONReader.loadJSONFromCommit("TransactionTests/tt10mbDataField.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json1, excluded);

        String json2 = JSONReader.loadJSONFromCommit("TransactionTests/ttTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json2, excluded);
    }

    @Ignore // RLPWrongByteEncoding and RLPLength preceding 0s errors left
    @Test
    public void testWrongRLPTestsFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("TransactionTests/ttWrongRLPTransaction.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Ignore // Wrong sender, wrong hash
    @Test
    public void testEip155VitaliksTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        SystemProperties.getDefault().setBlockchainConfig(new AbstractNetConfig() {{
            add(0, new FrontierConfig());
            add(1_150_000, new HomesteadConfig());
            add(2_457_000, new Eip150HFConfig(new DaoHFConfig()));
            add(2_700_000, new Eip160HFConfig(new DaoHFConfig()){
                @Override
                public Integer getChainId() {
                    return 18;
                }
            });
        }});
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTestEip155VitaliksTests.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }

    @Ignore // Wrong sender, wrong hash
    @Test
    public void testEip155VRuleTestFromGitHub() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        SystemProperties.getDefault().setBlockchainConfig(new AbstractNetConfig() {{
            add(0, new FrontierConfig());
            add(1_150_000, new HomesteadConfig());
            add(2_457_000, new Eip150HFConfig(new DaoHFConfig()));
            add(2_700_000, new Eip160HFConfig(new DaoHFConfig()){
                @Override
                public Integer getChainId() {
                    return 18;
                }
            });
        }});
        String json = JSONReader.loadJSONFromCommit("TransactionTests/EIP155/ttTransactionTestVRule.json", shacommit);
        GitHubJSONTestSuite.runGitHubJsonTransactionTest(json, excluded);
    }
}

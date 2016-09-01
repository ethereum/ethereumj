package org.ethereum.jsontestsuite;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.config.blockchain.HomesteadConfig;
import org.ethereum.config.net.AbstractNetConfig;
import org.ethereum.config.net.MainNetConfig;
import org.ethereum.jsontestsuite.suite.JSONReader;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ethereum.jsontestsuite.suite.JSONReader.getFileNamesForTreeSha;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitHubStateTest {

    //SHACOMMIT of tested commit, ethereum/tests.git
    public String shacommit = "f28ac81493281feec0b17290565cf74042893677";


    private long oldForkValue;

    @Before
    public void setup() {
        // TODO remove this after Homestead launch and shacommit update with actual block number
        // for this JSON test commit the Homestead block was defined as 900000
        SystemProperties.getDefault().setBlockchainConfig(new AbstractNetConfig() {{
            add(0, new FrontierConfig());
            add(1_150_000, new HomesteadConfig());
        }});
    }

    @After
    public void clean() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Ignore
    @Test // this method is mostly for hands-on convenient testing
    public void stSingleTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, "suicideSendEtherPostDeath");
    }

    @Test
    public void stExample() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stExample.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallCodes() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallDelegateCodes() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallDelegateCodes.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallDelegateCodesCallCode() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallDelegateCodesCallCode.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stHomeSteadSpecific() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stHomeSteadSpecific.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stCallCreateCallCodeTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        excluded.add("CallRecursiveBombPreCall"); // Max Gas value is pending to be < 2^63
        String json = JSONReader.loadJSONFromCommit("StateTests/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stCallCreateCallCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stDelegatecallTest() throws ParseException, IOException {

        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stDelegatecallTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stInitCodeTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stInitCodeTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stLogTests() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stLogTests.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stPreCompiledContracts() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stPreCompiledContracts.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stMemoryStressTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        excluded.add("mload32bitBound_return2");// The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_return"); // The test extends memory to 4Gb which can't be handled with Java arrays
        excluded.add("mload32bitBound_Msize"); // The test extends memory to 4Gb which can't be handled with Java arrays
        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stMemoryStressTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stMemoryTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stMemoryTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stQuadraticComplexityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        // leaving only Homestead version since the test runs too long
//        String json = JSONReader.loadJSONFromCommit("StateTests/stQuadraticComplexityTest.json", shacommit);
//        GitHubJSONTestSuite.runStateTest(json, excluded);

        String json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stQuadraticComplexityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stSolidityTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stSolidityTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stRecursiveCreate() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();
        String json = JSONReader.loadJSONFromCommit("StateTests/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stRecursiveCreate.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stRefundTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stRefundTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stSpecialTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stSpecialTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stBlockHashTest() throws ParseException, IOException {
        String json = JSONReader.loadJSONFromCommit("StateTests/stBlockHashTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json);
    }

    @Test
    public void stSystemOperationsTest() throws IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stSystemOperationsTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stTransactionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stTransactionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stTransitionTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stTransitionTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test
    public void stWalletTest() throws ParseException, IOException {
        Set<String> excluded = new HashSet<>();

        String json = JSONReader.loadJSONFromCommit("StateTests/stWalletTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);

        json = JSONReader.loadJSONFromCommit("StateTests/Homestead/stWalletTest.json", shacommit);
        GitHubJSONTestSuite.runStateTest(json, excluded);
    }

    @Test // testing full suite
    public void testRandomStateGitHub() throws ParseException, IOException {

        String sha = "99db6f4f5fea3aa5cfbe8436feba8e213d06d1e8";
        List<String> fileNames = getFileNamesForTreeSha(sha);
        List<String> includedFiles =
                Arrays.asList(
                        "st201504081841JAVA.json",
                        "st201504081842JAVA.json",
                        "st201504081843JAVA.json"
                );

        for (String fileName : fileNames) {
            if (includedFiles.contains(fileName)) {
              System.out.println("Running: " + fileName);
              String json = JSONReader.loadJSON("StateTests//RandomTests/" + fileName);
              GitHubJSONTestSuite.runStateTest(json);
            }
        }

    }
}


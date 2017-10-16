package org.ethereum.jsontestsuite;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

import static org.ethereum.jsontestsuite.GitHubJSONTestSuite.runTrieTest;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2017
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GithubTrieTest {

    String commitSHA = "7f638829311dfc1d341c1db85d8a891f57fa4da7";

    @Test
    public void hexEncodedSecureTrieTest() throws IOException {
        runTrieTest("TrieTests/hex_encoded_securetrie_test.json", commitSHA, true);
    }

    @Test
    public void trieAnyOrder() throws IOException {
        runTrieTest("TrieTests/trieanyorder.json", commitSHA, false);
    }

    @Test
    public void trieAnyOrderSecureTrie() throws IOException {
        runTrieTest("TrieTests/trieanyorder_secureTrie.json", commitSHA, true);
    }

    @Test
    public void trieTest() throws IOException {
        runTrieTest("TrieTests/trietest.json", commitSHA, false);
    }

    @Test
    public void trieTestSecureTrie() throws IOException {
        runTrieTest("TrieTests/trietest_secureTrie.json", commitSHA, true);
    }
}

/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
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

    String commitSHA = "253e99861fe406c7b1daf3d6a0c40906e8a8fd8f";

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

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
package org.ethereum.jsontestsuite.suite;

import org.ethereum.jsontestsuite.GitHubJSONTestSuite;

public class TransactionTestCase {
    private String rlp;
    private String expectedHash;
    private String expectedRlp;
    private GitHubJSONTestSuite.Network network;

    public TransactionTestCase() {
    }

    public String getRlp() {
        return rlp;
    }

    public void setRlp(String rlp) {
        this.rlp = rlp;
    }

    public String getExpectedHash() {
        return expectedHash;
    }

    public void setExpectedHash(String expectedHash) {
        this.expectedHash = expectedHash;
    }

    public String getExpectedRlp() {
        return expectedRlp;
    }

    public void setExpectedRlp(String expectedRlp) {
        this.expectedRlp = expectedRlp;
    }

    public GitHubJSONTestSuite.Network getNetwork() {
        return network;
    }

    public void setNetwork(GitHubJSONTestSuite.Network network) {
        this.network = network;
    }
}

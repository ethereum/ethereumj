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

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.jsontestsuite.GitHubJSONTestSuite;
import org.ethereum.jsontestsuite.suite.model.TransactionTck;
import org.ethereum.jsontestsuite.suite.model.AccountTck;
import org.ethereum.jsontestsuite.suite.model.EnvTck;

import java.util.Map;

public class StateTestCase {


    private EnvTck env;
    @JsonDeserialize(using = Logs.Deserializer.class)
    private Logs logs;
    private String out;
    private Map<String, AccountTck> pre;
    private String postStateRoot;
    private Map<String, AccountTck> post;
    private TransactionTck transaction;
    private GitHubJSONTestSuite.Network network;
    private String name;

    public StateTestCase() {
    }

    public EnvTck getEnv() {
        return env;
    }

    public void setEnv(EnvTck env) {
        this.env = env;
    }

    public Logs getLogs() {
        return logs;
    }

    public void setLogs(Logs logs) {
        this.logs = logs;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public Map<String, AccountTck> getPre() {
        return pre;
    }

    public void setPre(Map<String, AccountTck> pre) {
        this.pre = pre;
    }

    public String getPostStateRoot() {
        return postStateRoot;
    }

    public void setPostStateRoot(String postStateRoot) {
        this.postStateRoot = postStateRoot;
    }

    public Map<String, AccountTck> getPost() {
        return post;
    }

    public void setPost(Map<String, AccountTck> post) {
        this.post = post;
    }

    public TransactionTck getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionTck transaction) {
        this.transaction = transaction;
    }

    public GitHubJSONTestSuite.Network getNetwork() {
        return network;
    }

    public void setNetwork(GitHubJSONTestSuite.Network network) {
        this.network = network;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlockchainNetConfig getConfig() {
        return network.getConfig();
    }
}

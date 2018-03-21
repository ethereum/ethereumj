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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.ethereum.jsontestsuite.GitHubJSONTestSuite;
import org.ethereum.jsontestsuite.suite.model.*;

import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 09.08.2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateTestDataEntry {

    EnvTck env;
    Map<String, AccountTck> pre;
    Map<String, List<PostDataTck>> post;
    TransactionDataTck transaction;

    public EnvTck getEnv() {
        return env;
    }

    public void setEnv(EnvTck env) {
        this.env = env;
    }

    public Map<String, AccountTck> getPre() {
        return pre;
    }

    public void setPre(Map<String, AccountTck> pre) {
        this.pre = pre;
    }

    public Map<String, List<PostDataTck>> getPost() {
        return post;
    }

    public void setPost(Map<String, List<PostDataTck>> post) {
        this.post = post;
    }

    public TransactionDataTck getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionDataTck transaction) {
        this.transaction = transaction;
    }

    public List<StateTestCase> getTestCases(GitHubJSONTestSuite.Network network) {

        List<PostDataTck> postData = post.get(network.name());
        if (postData == null) return Collections.emptyList();

        List<StateTestCase> testCases = new ArrayList<>();
        for (PostDataTck data : postData) {
            StateTestCase testCase = new StateTestCase();

            testCase.setEnv(env);
            testCase.setPre(pre);
            if (data.getLogs() != null) {
                testCase.setLogs(data.getLogs());
            } else {
                testCase.setLogs(new Logs("0x"));
            }
            if (data.getHash() != null) {
                testCase.setPostStateRoot(data.getHash().startsWith("0x") ? data.getHash().substring(2) : data.getHash());
            }
            testCase.setTransaction(transaction.getTransaction(data.getIndexes()));
            testCase.setNetwork(network);

            testCases.add(testCase);
        }

        return testCases;
    }
}

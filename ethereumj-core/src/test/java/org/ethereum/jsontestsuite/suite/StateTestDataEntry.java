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

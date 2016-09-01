package org.ethereum.jsontestsuite.suite;

import org.ethereum.jsontestsuite.suite.model.TransactionTck;
import org.ethereum.jsontestsuite.suite.model.AccountTck;
import org.ethereum.jsontestsuite.suite.model.EnvTck;
import org.ethereum.jsontestsuite.suite.model.LogTck;

import java.util.List;
import java.util.Map;

public class StateTestCase {


    private EnvTck env;
    private List<LogTck> logs;
    private String out;
    private Map<String, AccountTck> pre;
    private String postStateRoot;
    private Map<String, AccountTck> post;
    private TransactionTck transaction;


    public StateTestCase() {
    }

    public EnvTck getEnv() {
        return env;
    }

    public void setEnv(EnvTck env) {
        this.env = env;
    }

    public List<LogTck> getLogs() {
        return logs;
    }

    public void setLogs(List<LogTck> logs) {
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
}

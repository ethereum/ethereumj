package org.ethereum.jsontestsuite.suite;

import org.ethereum.jsontestsuite.suite.model.TransactionTck;

public class TransactionTestCase {

    private String blocknumber;
    private TransactionTck transaction;
    private String hash;
    private String rlp;
    private String sender;
    private String senderExpect;


    public TransactionTestCase() {
    }

    public String getBlocknumber() {
        return blocknumber;
    }

    public void setBlocknumber(String blocknumber) {
        this.blocknumber = blocknumber;
    }

    public TransactionTck getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionTck transaction) {
        this.transaction = transaction;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRlp() {
        return rlp;
    }

    public void setRlp(String rlp) {
        this.rlp = rlp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderExpect() {
        return senderExpect;
    }

    public void setSenderExpect(String senderExpect) {
        this.senderExpect = senderExpect;
    }
}

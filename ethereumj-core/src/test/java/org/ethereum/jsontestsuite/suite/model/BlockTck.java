package org.ethereum.jsontestsuite.suite.model;

import java.util.List;

public class BlockTck {

    String acomment;
    BlockHeaderTck blockHeader;
    List<TransactionTck> transactions;
    List<BlockHeaderTck> uncleHeaders;
    String rlp;
    String blocknumber;
    String chainname;
    boolean reverted;

    public BlockTck() {
    }

    public String getBlocknumber() {
        return blocknumber;
    }

    public void setBlocknumber(String blocknumber) {
        this.blocknumber = blocknumber;
    }

    public String getChainname() {
        return chainname;
    }

    public void setChainname(String chainname) {
        this.chainname = chainname;
    }

    public BlockHeaderTck getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeaderTck blockHeader) {
        this.blockHeader = blockHeader;
    }

    public String getRlp() {
        return rlp;
    }

    public void setRlp(String rlp) {
        this.rlp = rlp;
    }

    public List<TransactionTck> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionTck> transactions) {
        this.transactions = transactions;
    }

    public List<BlockHeaderTck> getUncleHeaders() {
        return uncleHeaders;
    }

    public void setUncleHeaders(List<BlockHeaderTck> uncleHeaders) {
        this.uncleHeaders = uncleHeaders;
    }

    public boolean isReverted() {
        return reverted;
    }

    public void setReverted(boolean reverted) {
        this.reverted = reverted;
    }

    public String getAcomment() {
        return acomment;
    }

    public void setAcomment(String acomment) {
        this.acomment = acomment;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockHeader=" + blockHeader +
                ", transactions=" + transactions +
                ", uncleHeaders=" + uncleHeaders +
                ", rlp='" + rlp + '\'' +
                '}';
    }
}

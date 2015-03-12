package org.ethereum.jsontestsuite;

import java.util.List;

public class Block {

    BlockHeader blockHeader;
    List<Transaction2> transactions;
    List<BlockHeader> uncleHeaders;
    String rlp;

    public Block() {
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }

    public String getRlp() {
        return rlp;
    }

    public void setRlp(String rlp) {
        this.rlp = rlp;
    }

    public List<Transaction2> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction2> transactions) {
        this.transactions = transactions;
    }

    public List<BlockHeader> getUncleHeaders() {
        return uncleHeaders;
    }

    public void setUncleHeaders(List<BlockHeader> uncleHeaders) {
        this.uncleHeaders = uncleHeaders;
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

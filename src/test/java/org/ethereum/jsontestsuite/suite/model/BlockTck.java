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
package org.ethereum.jsontestsuite.suite.model;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties({"acomment", "comment", "chainname", "chainnetwork"})
public class BlockTck {

    BlockHeaderTck blockHeader;
    List<TransactionTck> transactions;
    List<BlockHeaderTck> uncleHeaders;
    String rlp;
    String blocknumber;
    boolean reverted;

    public BlockTck() {
    }

    public String getBlocknumber() {
        return blocknumber;
    }

    public void setBlocknumber(String blocknumber) {
        this.blocknumber = blocknumber;
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

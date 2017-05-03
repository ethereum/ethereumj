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
package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * The block in Ethereum is the collection of relevant pieces of information
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known
 * to have a parent equal to the present block’s parent’s parent
 * (such blocks are known as uncles).
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Block {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private IBlockHeader header;

    /* Transactions */
    private List<Transaction> transactionsList = new CopyOnWriteArrayList<>();

    /* Uncles */
    private List<IBlockHeader> uncleList = new CopyOnWriteArrayList<>();

    /* Private */

    private byte[] rlpEncoded;
    private boolean parsed = false;

    /* Constructors */


    public Block(byte... rawData) {
        getLogger().debug("new from [" + Hex.toHexString(rawData) + "]");
        this.setRlpEncoded(rawData);
    }

    public Block(IBlockHeader header, List<Transaction> transactionsList, List<IBlockHeader> uncleList) {

        this(header.getParentHash(),
                header.getUnclesHash(),
                header.getCoinbase(),
                header.getLogsBloom(),
                header.getDifficulty(),
                header.getNumber(),
                header.getGasLimit(),
                header.getGasUsed(),
                header.getTimestamp(),
                header.getMixHash(), header.getNonce(), header.getReceiptsRoot(), header.getTxTrieRoot(), header.getStateRoot(), transactionsList, uncleList, header.getExtraData()
        );
    }

    public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
                 byte[] difficulty, long number, byte[] gasLimit,
                 long gasUsed, long timestamp, byte[] mixHash, byte[] nonce, byte[] receiptsRoot, byte[] transactionsRoot, byte[] stateRoot, List<Transaction> transactionsList, List<IBlockHeader> uncleList, byte... extraData) {

        this(parentHash, unclesHash, coinbase, logsBloom, difficulty, number, gasLimit,
                gasUsed, timestamp, mixHash, nonce, transactionsList, uncleList, extraData);

        this.getHeader().setTransactionsRoot(BlockchainImpl.calcTxTrie(transactionsList));
        if (!Hex.toHexString(transactionsRoot).
                equals(Hex.toHexString(this.getHeader().getTxTrieRoot())))
            getLogger().debug("Transaction root miss-calculate, block: {}", getNumber());

        this.getHeader().setStateRoot(stateRoot);
        this.getHeader().setReceiptsRoot(receiptsRoot);
    }


    public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
                 byte[] difficulty, long number, byte[] gasLimit,
                 long gasUsed, long timestamp,
                 byte[] mixHash, byte[] nonce, List<Transaction> transactionsList, List<IBlockHeader> uncleList, byte... extraData) {
        this.setHeader(IBlockHeader.Factory.assembleBlockHeader(parentHash, unclesHash, coinbase, logsBloom,
                difficulty, number, gasLimit, gasUsed,
                timestamp, mixHash, nonce, extraData));

        this.setTransactionsList(transactionsList);
        if (this.getTransactionsList() == null) {
            this.setTransactionsList(new CopyOnWriteArrayList<>());
        }

        this.setUncleList(uncleList);
        if (this.getUncleList() == null) {
            this.setUncleList(new CopyOnWriteArrayList<>());
        }

        this.setParsed(true);
    }

    public static Logger getLogger() {
        return logger;
    }

    private synchronized void parseRLP() {
        if (isParsed()) return;

        RLPList params = RLP.decode2(getRlpEncoded());
        RLPList block = (RLPList) params.get(0);

        // Parse Header
        RLPList header = (RLPList) block.get(0);
        this.setHeader(IBlockHeader.Factory.decodeBlockHeader(header));

        // Parse Transactions
        RLPList txTransactions = (RLPList) block.get(1);
        this.parseTxs(this.getHeader().getTxTrieRoot(), txTransactions, false);

        // Parse Uncles
        RLPList uncleBlocks = (RLPList) block.get(2);
        for (RLPElement rawUncle : uncleBlocks) {

            RLPList uncleHeader = (RLPList) rawUncle;
            IBlockHeader blockData = IBlockHeader.Factory.decodeBlockHeader(uncleHeader);
            this.getUncleList().add(blockData);
        }
        this.setParsed(true);
    }

    public IBlockHeader getHeader() {
        parseRLP();
        return this.header;
    }

    public byte[] getHash() {
        parseRLP();
        return this.getHeader().getHash();
    }

    public byte[] getParentHash() {
        parseRLP();
        return this.getHeader().getParentHash();
    }

    public byte[] getUnclesHash() {
        parseRLP();
        return this.getHeader().getUnclesHash();
    }

    public byte[] getCoinbase() {
        parseRLP();
        return this.getHeader().getCoinbase();
    }

    public byte[] getStateRoot() {
        parseRLP();
        return this.getHeader().getStateRoot();
    }

    public void setStateRoot(byte[] stateRoot) {
        parseRLP();
        this.getHeader().setStateRoot(stateRoot);
    }

    public byte[] getTxTrieRoot() {
        parseRLP();
        return this.getHeader().getTxTrieRoot();
    }

    public byte[] getReceiptsRoot() {
        parseRLP();
        return this.getHeader().getReceiptsRoot();
    }


    public byte[] getLogBloom() {
        parseRLP();
        return this.getHeader().getLogsBloom();
    }

    public byte[] getDifficulty() {
        parseRLP();
        return this.getHeader().getDifficulty();
    }

    public BigInteger getDifficultyBI() {
        parseRLP();
        return this.getHeader().getDifficultyBI();
    }


    public BigInteger getCumulativeDifficulty() {
        parseRLP();
        BigInteger calcDifficulty = new BigInteger(1, this.getHeader().getDifficulty());
        for (IBlockHeader uncle : getUncleList()) {
            calcDifficulty = calcDifficulty.add(new BigInteger(1, uncle.getDifficulty()));
        }
        return calcDifficulty;
    }

    public long getTimestamp() {
        parseRLP();
        return this.getHeader().getTimestamp();
    }

    public long getNumber() {
        parseRLP();
        return this.getHeader().getNumber();
    }

    public byte[] getGasLimit() {
        parseRLP();
        return this.getHeader().getGasLimit();
    }

    public long getGasUsed() {
        parseRLP();
        return this.getHeader().getGasUsed();
    }


    public byte[] getExtraData() {
        parseRLP();
        return this.getHeader().getExtraData();
    }

    public byte[] getMixHash() {
        parseRLP();
        return this.getHeader().getMixHash();
    }


    public byte[] getNonce() {
        parseRLP();
        return this.getHeader().getNonce();
    }

    public void setNonce(byte[] nonce) {
        this.getHeader().setNonce(nonce);
        setRlpEncoded(null);
    }

    public void setMixHash(byte[] hash) {
        this.getHeader().setMixHash(hash);
        setRlpEncoded(null);
    }

    public void setExtraData(byte[] data) {
        this.getHeader().setExtraData(data);
        setRlpEncoded(null);
    }

    public List<Transaction> getTransactionsList() {
        parseRLP();
        return transactionsList;
    }

    public List<IBlockHeader> getUncleList() {
        parseRLP();
        return uncleList;
    }

    private StringBuffer toStringBuff = new StringBuffer();
    // [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
    // difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,
    // extradata, nonce]

    @Override
    public String toString() {
        parseRLP();

        getToStringBuff().setLength(0);
        getToStringBuff().append(Hex.toHexString(this.getEncoded())).append("\n");
        getToStringBuff().append("BlockData [ ");
        getToStringBuff().append("hash=").append(ByteUtil.toHexString(this.getHash())).append("\n");
        getToStringBuff().append(getHeader().toString());

        if (!getUncleList().isEmpty()) {
            getToStringBuff().append("Uncles [\n");
            for (IBlockHeader uncle : getUncleList()) {
                getToStringBuff().append(uncle.toString());
                getToStringBuff().append("\n");
            }
            getToStringBuff().append("]\n");
        } else {
            getToStringBuff().append("Uncles []\n");
        }
        if (!getTransactionsList().isEmpty()) {
            getToStringBuff().append("Txs [\n");
            for (Transaction tx : getTransactionsList()) {
                getToStringBuff().append(tx);
                getToStringBuff().append("\n");
            }
            getToStringBuff().append("]\n");
        } else {
            getToStringBuff().append("Txs []\n");
        }
        getToStringBuff().append("]");

        return getToStringBuff().toString();
    }

    public String toFlatString() {
        parseRLP();

        getToStringBuff().setLength(0);
        getToStringBuff().append("BlockData [");
        getToStringBuff().append("hash=").append(ByteUtil.toHexString(this.getHash()));
        getToStringBuff().append(IBlockHeader.View.toFlatString(getHeader()));

        for (Transaction tx : getTransactionsList()) {
            getToStringBuff().append("\n");
            getToStringBuff().append(tx.toString());
        }

        getToStringBuff().append("]");
        return getToStringBuff().toString();
    }

    private byte[] parseTxs(RLPList txTransactions, boolean validate) {

        Trie<byte[]> txsState = new TrieImpl();
        for (int i = 0; i < txTransactions.size(); i++) {
            RLPElement transactionRaw = txTransactions.get(i);
            Transaction tx = new Transaction(transactionRaw.getRLPData());
            if (validate) tx.verify();
            this.getTransactionsList().add(tx);
            txsState.put(RLP.encodeInt(i), transactionRaw.getRLPData());
        }
        return txsState.getRootHash();
    }


    private boolean parseTxs(byte[] expectedRoot, RLPList txTransactions, boolean validate) {

        byte[] rootHash = parseTxs(txTransactions, validate);
        String calculatedRoot = Hex.toHexString(rootHash);
        if (!calculatedRoot.equals(Hex.toHexString(expectedRoot))) {
            getLogger().debug("Transactions trie root validation failed for block #{}", this.getHeader().getNumber());
            return false;
        }

        return true;
    }

    /**
     * check if param block is son of this block
     *
     * @param block - possible a son of this
     * @return - true if this block is parent of param block
     */
    public boolean isParentOf(Block block) {
        return Arrays.areEqual(this.getHash(), block.getParentHash());
    }

    public boolean isGenesis() {
        return this.getHeader().isGenesis();
    }

    public boolean isEqual(Block block) {
        return Arrays.areEqual(this.getHash(), block.getHash());
    }

    private byte[] getTransactionsEncoded() {

        byte[][] transactionsEncoded = new byte[getTransactionsList().size()][];
        int i = 0;
        for (Transaction tx : getTransactionsList()) {
            transactionsEncoded[i] = tx.getEncoded();
            ++i;
        }
        return RLP.encodeList(transactionsEncoded);
    }

    private byte[] getUnclesEncoded() {

        byte[][] unclesEncoded = new byte[getUncleList().size()][];
        int i = 0;
        for (IBlockHeader uncle : getUncleList()) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    public void addUncle(IBlockHeader uncle) {
        getUncleList().add(uncle);
        this.getHeader().setUnclesHash(sha3(getUnclesEncoded()));
        setRlpEncoded(null);
    }

    public byte[] getEncoded() {
        if (getRlpEncoded() == null) {
            byte[] header = this.getHeader().getEncoded();

            List<byte[]> block = getBodyElements();
            block.add(0, header);
            byte[][] elements = block.toArray(new byte[block.size()][]);

            this.setRlpEncoded(RLP.encodeList(elements));
        }
        return getRlpEncoded();
    }

    public byte[] getEncodedWithoutNonce() {
        parseRLP();
        return this.getHeader().getEncodedWithoutNonce();
    }

    public byte[] getEncodedBody() {
        List<byte[]> body = getBodyElements();
        byte[][] elements = body.toArray(new byte[body.size()][]);
        return RLP.encodeList(elements);
    }

    private List<byte[]> getBodyElements() {
        parseRLP();

        byte[] transactions = getTransactionsEncoded();
        byte[] uncles = getUnclesEncoded();

        List<byte[]> body = new ArrayList<>();
        body.add(transactions);
        body.add(uncles);

        return body;
    }

    public String getShortHash() {
        parseRLP();
        return Hex.toHexString(getHash()).substring(0, 6);
    }

    public String getShortDescr() {
        return "#" + getNumber() + " (" + Hex.toHexString(getHash()).substring(0,6) + " <~ "
                + Hex.toHexString(getParentHash()).substring(0,6) + ") Txs:" + getTransactionsList().size() +
                ", Unc: " + getUncleList().size();
    }

    public void setHeader(IBlockHeader header) {
        this.header = header;
    }

    public void setTransactionsList(List<Transaction> transactionsList) {
        this.transactionsList = transactionsList;
    }

    public void setUncleList(List<IBlockHeader> uncleList) {
        this.uncleList = uncleList;
    }

    public byte[] getRlpEncoded() {
        return rlpEncoded;
    }

    public void setRlpEncoded(byte[] rlpEncoded) {
        this.rlpEncoded = rlpEncoded;
    }

    public boolean isParsed() {
        return parsed;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    public StringBuffer getToStringBuff() {
        return toStringBuff;
    }

    public void setToStringBuff(StringBuffer toStringBuff) {
        this.toStringBuff = toStringBuff;
    }

    public static class Builder {

        private IBlockHeader header;
        private byte[] body;

        public Builder withHeader(IBlockHeader header) {
            this.header = header;
            return this;
        }

        public Builder withBody(byte[] body) {
            this.body = body;
            return this;
        }

        public Block create() {
            if (header == null || body == null) {
                return null;
            }

            Block block = new Block();
            block.setHeader(header);
            block.setParsed(true);

            RLPList items = (RLPList) RLP.decode2(body).get(0);

            RLPList transactions = (RLPList) items.get(0);
            RLPList uncles = (RLPList) items.get(1);

            if (!block.parseTxs(header.getTxTrieRoot(), transactions, false)) {
                return null;
            }

            byte[] unclesHash = HashUtil.sha3(uncles.getRLPData());
            if (!java.util.Arrays.equals(header.getUnclesHash(), unclesHash)) {
                return null;
            }

            for (RLPElement rawUncle : uncles) {

                RLPList uncleHeader = (RLPList) rawUncle;
                IBlockHeader blockData = IBlockHeader.Factory.decodeBlockHeader(uncleHeader);
                block.getUncleList().add(blockData);
            }

            return block;
        }
    }
}

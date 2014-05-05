package org.ethereum.net.vo;

import org.ethereum.crypto.HashUtil;
import org.ethereum.net.rlp.RLPElement;
import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;
import org.ethereum.util.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 13/04/14 19:34
 */
public class Block {

	private RLPList rawData;
    private boolean parsed = false;

    private byte[] hash;
    private byte[] parentHash;
    private byte[] unclesHash;
    private byte[] coinbase;
    private byte[] stateHash;
    private byte[] txListHash;
    private byte[] difficulty;

    private long timestamp;
    private byte[] extraData;
    private byte[] nonce;

    private List<Transaction> transactionsList = new ArrayList<Transaction>();
    private List<Block> uncleList = new ArrayList<Block>();

    public Block(RLPList rawData) {
        this.rawData = rawData;
        this.parsed = false;
    }

    public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] stateHash, byte[] txListHash, byte[] difficulty, long timestamp, byte[] extraData, byte[] nonce, List<Transaction> transactionsList, List<Block> uncleList) {
        this.parentHash = parentHash;
        this.unclesHash = unclesHash;
        this.coinbase = coinbase;
        this.stateHash = stateHash;
        this.txListHash = txListHash;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.nonce = nonce;
        this.transactionsList = transactionsList;
        this.uncleList = uncleList;
        this.parsed = true;
    }

    // [parent_hash,  uncles_hash, coinbase, state_root, tx_list_hash, difficulty, timestamp, extradata, nonce]
    private void parseRLP(){

        this.hash = HashUtil.sha3(rawData.getRLPData());

        List params = ((RLPList) rawData.getElement(0)).getList();

        this.parentHash      = ((RLPItem) params.get(0)).getData();
        this.unclesHash      = ((RLPItem) params.get(1)).getData();
        this.coinbase        = ((RLPItem) params.get(2)).getData();
        this.stateHash       = ((RLPItem) params.get(3)).getData();
        this.txListHash      = ((RLPItem) params.get(4)).getData();
        this.difficulty      = ((RLPItem) params.get(5)).getData();

        byte[] tsBytes       = ((RLPItem) params.get(6)).getData();
        this.timestamp       =  (new BigInteger(tsBytes)).longValue();
        this.extraData       = ((RLPItem) params.get(7)).getData();
        this.nonce           = ((RLPItem) params.get(8)).getData();

        // parse transactions
        List<RLPElement> transactions = ((RLPList) rawData.getElement(1)).getList();
        for (RLPElement rlpTx : transactions){
            Transaction tx = new Transaction((RLPList)rlpTx);
            this.transactionsList.add(tx);
        }
        // parse uncles
        List<RLPElement> uncleBlocks = ((RLPList) rawData.getElement(2)).getList();
        for (RLPElement rawUncle : uncleBlocks){
            Block blockData = new Block((RLPList)rawUncle);
            this.uncleList.add(blockData);
        }
        this.parsed = true;
    }

    public byte[] getHash(){

        if (!parsed) parseRLP();
        return hash;
    }

    public byte[] getParentHash() {
        if (!parsed) parseRLP();
        return parentHash;
    }

    public byte[] getUnclesHash() {
        if (!parsed) parseRLP();
        return unclesHash;
    }

    public byte[] getCoinbase() {
        if (!parsed) parseRLP();
        return coinbase;
    }

    public byte[] getStateHash() {
        if (!parsed) parseRLP();
        return stateHash;
    }

    public byte[] getTxListHash() {
        if (!parsed) parseRLP();
        return txListHash;
    }

    public byte[] getDifficulty() {
        if (!parsed) parseRLP();
        return difficulty;
    }

    public long getTimestamp() {
        if (!parsed) parseRLP();
        return timestamp;
    }

    public byte[] getExtraData() {
        if (!parsed) parseRLP();
        return extraData;
    }

    public byte[] getNonce() {
        if (!parsed) parseRLP();
        return nonce;
    }

    public List<Transaction> getTransactionsList() {
        if (!parsed) parseRLP();
        return transactionsList;
    }

    public List<Block> getUncleList() {
        if (!parsed) parseRLP();
        return uncleList;
    }

    // [parent_hash,  uncles_hash, coinbase, state_root, tx_list_hash, difficulty, timestamp, extradata, nonce]
    @Override
    public String toString() {
        if (!parsed) parseRLP();

        return "BlockData [" +  " hash=" + Utils.toHexString(hash) +
                "  parentHash=" + Utils.toHexString(parentHash) +
                ", unclesHash=" + Utils.toHexString(unclesHash) +
                ", coinbase=" + Utils.toHexString(coinbase) +
                ", stateHash=" + Utils.toHexString(stateHash) +
                ", txListHash=" + Utils.toHexString(txListHash) +
                ", difficulty=" + Utils.toHexString(difficulty) +
                ", timestamp=" + timestamp +
                ", extraData=" + Utils.toHexString(extraData) +
                ", nonce=" + Utils.toHexString(nonce) +
                ']';
    }
}

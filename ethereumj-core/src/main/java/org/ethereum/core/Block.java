package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The block in Ethereum is the collection of relevant pieces of information
 * (known as the blockheader), H, together with information corresponding to
 * the comprised transactions, R, and a set of other blockheaders U that are known
 * to have a parent equal to the present block’s parent’s parent
 * (such blocks are known as uncles).
 *
 * www.ethereumJ.com
 *
 * @author Roman Mandeleil
 * @author Nick Savers
 * @since 20.05.2014
 */
public class Block {

    private static final Logger logger = LoggerFactory.getLogger("block");

    public static BigInteger BLOCK_REWARD = BigInteger.valueOf(1500000000000000000L);
    public static BigInteger UNCLE_REWARD = BLOCK_REWARD.multiply(
            BigInteger.valueOf(15)).divide(BigInteger.valueOf(16));
    public static BigInteger INCLUSION_REWARD = Block.BLOCK_REWARD
            .divide(BigInteger.valueOf(32));

    private BlockHeader header;

    /* Transactions */
    private List<Transaction> transactionsList = new CopyOnWriteArrayList<>();

    /* Uncles */
    private List<BlockHeader> uncleList = new CopyOnWriteArrayList<>();

    /* Private */

    private byte[] rlpEncoded;
    private boolean parsed = false;

    private Trie txsState;

    /* Constructors */

    public Block(byte[] rawData) {
        logger.debug("new from [" + Hex.toHexString(rawData) + "]");
        this.rlpEncoded = rawData;
        this.parsed = false;
    }

    public Block(byte[] parentHash, byte[] unclesHash, byte[] coinbase, byte[] logsBloom,
            byte[] difficulty, long number, long gasLimit,
            long gasUsed, long timestamp, byte[] extraData, byte[] nonce,
            List<Transaction> transactionsList, List<BlockHeader> uncleList) {
        this.header = new BlockHeader(parentHash, unclesHash, coinbase, logsBloom,
                difficulty, number, gasLimit, gasUsed,
                timestamp, extraData, nonce);

        this.transactionsList = transactionsList;
        if (this.transactionsList == null) {
            this.transactionsList = new CopyOnWriteArrayList<Transaction>();
        }

        this.uncleList = uncleList;
        if (this.uncleList == null) {
            this.uncleList = new CopyOnWriteArrayList<BlockHeader>();
        }

        this.parsed = true;
    }

    private void parseRLP() {

        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);

        // Parse Header
        RLPList header = (RLPList) block.get(0);
        this.header = new BlockHeader(header);

        // Parse Transactions
        RLPList txTransactions = (RLPList) block.get(1);
        this.parseTxs(this.header.getTxTrieRoot(), txTransactions);

        // Parse Uncles
        RLPList uncleBlocks = (RLPList) block.get(2);
        for (RLPElement rawUncle : uncleBlocks) {

            RLPList uncleHeader = (RLPList) rawUncle;
            BlockHeader blockData = new BlockHeader(uncleHeader);
            this.uncleList.add(blockData);
        }
        this.parsed = true;
    }

    public BlockHeader getHeader() {
        if (!parsed) parseRLP();
        return this.header;
    }

    public byte[] getHash() {
        if (!parsed) parseRLP();
        return HashUtil.sha3(this.header.getEncoded());
    }


    public byte[] calcDifficulty() {
        if (!parsed) parseRLP();
        return this.header.calcDifficulty();
    }

    public boolean validateNonce() {
        if (!parsed) parseRLP();
        BigInteger max = BigInteger.valueOf(2).pow(256);
        byte[] target = BigIntegers.asUnsignedByteArray(32, max.divide(new BigInteger(1, this.getDifficulty())));
        byte[] hash = HashUtil.sha3(this.getEncodedWithoutNonce());
        byte[] concat = Arrays.concatenate(hash, this.getNonce());
        byte[] result = HashUtil.sha3(concat);
        return FastByteComparisons.compareTo(result, 0, 32, target, 0, 32) < 0;
    }


    public byte[] getParentHash() {
        if (!parsed) parseRLP();
        return this.header.getParentHash();
    }

    public byte[] getUnclesHash() {
        if (!parsed) parseRLP();
        return this.header.getUnclesHash();
    }

    public byte[] getCoinbase() {
        if (!parsed) parseRLP();
        return this.header.getCoinbase();
    }

    public byte[] getStateRoot() {
        if (!parsed) parseRLP();
        return this.header.getStateRoot();
    }

    public void setStateRoot(byte[] stateRoot) {
        if (!parsed) parseRLP();
        this.header.setStateRoot(stateRoot);
    }

    public byte[] getTxTrieRoot() {
        if (!parsed) parseRLP();
        return this.header.getTxTrieRoot();
    }

    public byte[] getLogBloom() {
        if (!parsed) parseRLP();
        return this.header.getLogsBloom();
    }

    public byte[] getDifficulty() {
        if (!parsed) parseRLP();
        return this.header.getDifficulty();
    }

    public BigInteger getCumulativeDifficulty() {
        if (!parsed) parseRLP();
        BigInteger calcDifficulty = new BigInteger(1, this.header.getDifficulty());
        for (BlockHeader uncle : uncleList) {
            calcDifficulty = calcDifficulty.add(new BigInteger(1, uncle.getDifficulty()));
        }
        return calcDifficulty;
    }

    public long getTimestamp() {
        if (!parsed) parseRLP();
        return this.header.getTimestamp();
    }

    public long getNumber() {
        if (!parsed) parseRLP();
        return this.header.getNumber();
    }

    public long getGasLimit() {
        if (!parsed) parseRLP();
        return this.header.getGasLimit();
    }

    public long getGasUsed() {
        if (!parsed) parseRLP();
        return this.header.getGasUsed();
    }

    public byte[] getExtraData() {
        if (!parsed) parseRLP();
        return this.header.getExtraData();
    }

    public byte[] getNonce() {
        if (!parsed) parseRLP();
        return this.header.getNonce();
    }

    public void setNonce(byte[] nonce) {
        this.header.setNonce(nonce);
        rlpEncoded = null;
    }

    public List<Transaction> getTransactionsList() {
        if (!parsed) parseRLP();
        return transactionsList;
    }

    public List<BlockHeader> getUncleList() {
        if (!parsed) parseRLP();
        return uncleList;
    }

    private StringBuffer toStringBuff = new StringBuffer();
    // [parent_hash, uncles_hash, coinbase, state_root, tx_trie_root,
    // difficulty, number, minGasPrice, gasLimit, gasUsed, timestamp,
    // extradata, nonce]

    @Override
    public String toString() {

        if (!parsed) parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append(Hex.toHexString(this.getEncoded())).append("\n");
        toStringBuff.append("BlockData [ ");
        toStringBuff.append("hash=" + ByteUtil.toHexString(this.getHash())).append("\n");
        toStringBuff.append(header.toString());

        toStringBuff.append("\nUncles [\n");
        for (BlockHeader uncle : getUncleList()) {
            toStringBuff.append(uncle.toString());
            toStringBuff.append("\n");
        }
        toStringBuff.append("]");
        toStringBuff.append("\n]");

        return toStringBuff.toString();
    }

    public String toFlatString() {
        if (!parsed) parseRLP();

        toStringBuff.setLength(0);
        toStringBuff.append("BlockData [");
        toStringBuff.append("hash=" + ByteUtil.toHexString(this.getHash())).append("");
        toStringBuff.append(header.toFlatString());

        for (Transaction tx : getTransactionsList()) {
            toStringBuff.append("\n");
            toStringBuff.append(tx.toString());
        }

        toStringBuff.append("]");
        return toStringBuff.toString();
    }


    private void parseTxs(byte[] expectedRoot, RLPList txTransactions) {

        this.txsState = new TrieImpl(null);
        for (int i = 0; i < txTransactions.size(); i++) {
            RLPElement transactionRaw = txTransactions.get(i);
            this.transactionsList.add(new Transaction(transactionRaw.getRLPData()));
            this.txsState.update(RLP.encodeInt(i), transactionRaw.getRLPData());
        }

        String calculatedRoot = Hex.toHexString(txsState.getRootHash());
        if (!calculatedRoot.equals(Hex.toHexString(expectedRoot)))
            logger.error("Added tx receipts don't match the given txsStateRoot");
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
        return this.header.isGenesis();
    }

    public boolean isEqual(Block block) {
        return Arrays.areEqual(this.getHash(), block.getHash());
    }

    private byte[] getUnclesEncoded() {

        byte[][] unclesEncoded = new byte[uncleList.size()][];
        int i = 0;
        for (BlockHeader uncle : uncleList) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        return RLP.encodeList(unclesEncoded);
    }

    public void addUncle(BlockHeader uncle) {
        uncleList.add(uncle);
        this.getHeader().setUnclesHash(SHA3Helper.sha3(getUnclesEncoded()));
        rlpEncoded = null;
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] header = this.header.getEncoded();
            byte[] transactions = RLP.encodeList();
            byte[] uncles = getUnclesEncoded();
            this.rlpEncoded = RLP.encodeList(header, transactions, uncles);
        }
        return rlpEncoded;
    }

    public byte[] getEncodedWithoutNonce() {
        if (!parsed) parseRLP();
        byte[] header = this.header.getEncodedWithoutNonce();
        return header;
    }

    public String getShortHash() {
        if (!parsed) parseRLP();
        return Hex.toHexString(getHash()).substring(0, 6);
    }
}

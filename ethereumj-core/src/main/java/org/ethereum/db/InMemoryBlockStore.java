package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigInteger.ZERO;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * @author: Roman Mandeleil
 * Created on: 29/01/2015 20:43
 */

public class InMemoryBlockStore implements BlockStore{

    private static final Logger logger = LoggerFactory.getLogger("general");

    Map<ByteArrayWrapper, Block> hashIndex = new HashMap<>();
    Map<Long, Block> numberIndex = new HashMap<>();
    List<Block> blocks = new ArrayList<>();

    SessionFactory sessionFactory;

    BigInteger totalDifficulty = ZERO;

    public InMemoryBlockStore(){
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {

        Block block = numberIndex.get(blockNumber);

        if (block == null)
            return dbGetBlockHashByNumber(blockNumber);
        else
            return block.getHash();
    }

    @Override
    public Block getBlockByNumber(long blockNumber) {

        Block block = numberIndex.get(blockNumber);

        if (block == null)
            return dbGetBlockByNumber(blockNumber);
        else
            return block;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {

        Block block = hashIndex.get(wrap(hash));

        if (block == null)
            return dbGetBlockByHash(hash);
        else
            return block;
    }

    @Override
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {

        Block startBlock = hashIndex.get(wrap(hash));

        long endIndex = startBlock.getNumber() + qty;
        endIndex = getBestBlock().getNumber() < endIndex ? getBestBlock().getNumber() : endIndex;

        List<byte[]> hashes = new ArrayList<>();

        for (long i = startBlock.getNumber();  i <= endIndex; ++i){
            Block block = getBlockByNumber(i);
            hashes.add(block.getHash() );
        }

        return hashes;
    }

    @Override
    public void deleteBlocksSince(long number) {

        // todo: delete blocks sinse
    }

    @Override
    public void saveBlock(Block block, List<TransactionReceipt> receipts) {
        ByteArrayWrapper wHash = wrap(block.getHash());
        blocks.add(block);
        hashIndex.put(wHash, block);
        numberIndex.put(block.getNumber(), block);
        totalDifficulty = totalDifficulty.add(block.getCumulativeDifficulty());
    }

    @Override
    public BigInteger getTotalDifficultySince(long number) {

        // todo calculate from db + from cache

        throw new UnsupportedOperationException();
    }

    @Override
    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    @Override
    public Block getBestBlock() {
        if (blocks.size() == 0) return null;
        return blocks.get(blocks.size() - 1);
    }

    @Override
    public List<Block> getAllBlocks() {
        return blocks;
    }

    @Override
    public void reset() {
        blocks.clear();
        hashIndex.clear();
        numberIndex.clear();
    }

    @Override
    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {
        return null;
    }

    // FIXME: wrap from here in to db class

    public byte[] dbGetBlockHashByNumber(long blockNumber) {

        Session s = sessionFactory.openSession();

        List result = s.createQuery("from BlockVO where number = :number").
                setParameter("number", blockNumber).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return vo.getHash();
    }

    public Block dbGetBlockByNumber(long blockNumber) {

        Session s = sessionFactory.openSession();

        List result = s.createQuery("from BlockVO where number = :number").
                setParameter("number", blockNumber).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        s.close();

        byte[] rlp = vo.getRlp();
        return new Block(rlp);
    }

    public Block dbGetBlockByHash(byte[] hash) {

        Session s = sessionFactory.openSession();

        List result = s.createQuery("from BlockVO where hash = :hash").
                setParameter("hash", hash).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        s.close();
        return new Block(vo.rlp);
    }

    @Override
    public void flush(){

        long t_ = System.nanoTime();

        Session s = sessionFactory.openSession();
        s.beginTransaction();
        for (Block block : blocks){
            BlockVO blockVO = new BlockVO(block.getNumber(), block.getHash(), block.getEncoded(), block.getCumulativeDifficulty());
            s.saveOrUpdate(blockVO);
        }

        s.getTransaction().commit();

        Block block = getBestBlock();

        blocks.clear();
        hashIndex.clear();
        numberIndex.clear();

        saveBlock(block, null);

        long t__ = System.nanoTime();
        logger.info("Flush block store in: {} ms", ((float)(t__ - t_) / 1_000_000));

        totalDifficulty =  (BigInteger) s.createQuery("select sum(cumulativeDifficulty) from BlockVO").uniqueResult();

        s.close();
    }

    public void load(){

        logger.info("loading db");

        long t = System.nanoTime();
        Session s = sessionFactory.openSession();

        Long bestNumber = (Long)
                s.createQuery("select max(number) from BlockVO").uniqueResult();

        List result =
                s.createQuery("from BlockVO where number = :number").setParameter("number", bestNumber).list();

        if (result.isEmpty()) return ;
        BlockVO vo = (BlockVO) result.get(0);

        Block bestBlock = new Block(vo.rlp);
        saveBlock(bestBlock, null);

        totalDifficulty =  (BigInteger) s.createQuery("select sum(cumulativeDifficulty) from BlockVO").uniqueResult();

        long t_ = System.nanoTime();

        logger.info("Loaded db in: {} ms", ((float)(t_ - t) / 1_000_000));
    }

    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}


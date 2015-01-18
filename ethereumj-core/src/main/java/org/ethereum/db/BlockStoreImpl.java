package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

import org.ethereum.util.ByteUtil;
import org.hibernate.SessionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Mandeleil
 * @since 12.11.2014
 */
@Repository("blockStore")
@Transactional(propagation = Propagation.SUPPORTS)
public class BlockStoreImpl implements BlockStore{

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    ApplicationContext ctx;

    public BlockStoreImpl() {
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {

        Block block = getBlockByNumber(blockNumber);
        if (block != null) return block.getHash();
        return ByteUtil.EMPTY_BYTE_ARRAY;
    }

    @Override
    @Transactional(readOnly = true)
    public Block getBlockByNumber(long blockNumber) {

        List result = sessionFactory.getCurrentSession().
                createQuery("from BlockVO where number = :number").
                setParameter("number", blockNumber).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
    }

    @Override
    @Transactional(readOnly = true)
    public Block getBlockByHash(byte[] hash) {

        List result = sessionFactory.getCurrentSession().
                createQuery("from BlockVO where hash = :hash").
                setParameter("hash", hash).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {

        List<byte[]> hashes = new ArrayList<>();

        // find block number of that block hash
        Block block = getBlockByHash(hash);
        if (block == null) return hashes;

        List<byte[]> result = sessionFactory.getCurrentSession().
                createQuery("select hash from BlockVO where number <= :number and number >= :limit order by number desc").
                setParameter("number", block.getNumber()).
                setParameter("limit", block.getNumber() - qty).
                setMaxResults(qty).list();

        for (byte[] h : result)
            hashes.add(h);

        return hashes;
    }

    @Override
    @Transactional
    public void deleteBlocksSince(long number) {

        sessionFactory.getCurrentSession().
                createQuery("delete from BlockVO where number > :number").
                setParameter("number", number).
                executeUpdate();
    }


    @Override
    @Transactional
    public void saveBlock(Block block, List<TransactionReceipt> receipts) {

        BlockVO blockVO = new BlockVO(block.getNumber(), block.getHash(),
                block.getEncoded(), block.getCumulativeDifficulty());

        for (TransactionReceipt receipt : receipts) {

            byte[] hash = receipt.getTransaction().getHash();
            byte[] rlp = receipt.getEncoded();

            TransactionReceiptVO transactionReceiptVO = new TransactionReceiptVO(hash, rlp);
            sessionFactory.getCurrentSession().persist(transactionReceiptVO);
        }

        sessionFactory.getCurrentSession().persist(blockVO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigInteger getTotalDifficultySince(long number) {

        return (BigInteger) sessionFactory.getCurrentSession().
                createQuery("select sum(cumulativeDifficulty) from BlockVO where number > :number").
                setParameter("number", number).
                uniqueResult();
    }


    @Override
    @Transactional(readOnly = true)
    public BigInteger getTotalDifficulty() {

        return (BigInteger) sessionFactory.getCurrentSession().
                createQuery("select sum(cumulativeDifficulty) from BlockVO").uniqueResult();
    }


    @Override
    @Transactional(readOnly = true)
    public Block getBestBlock() {

        Long bestNumber = (Long)
                sessionFactory.getCurrentSession().createQuery("select max(number) from BlockVO").uniqueResult();
        List result = sessionFactory.getCurrentSession().
                createQuery("from BlockVO where number = :number").setParameter("number", bestNumber).list();

        if (result.isEmpty()) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Block> getAllBlocks() {

        List<BlockVO> result = sessionFactory.getCurrentSession().
                createQuery("from BlockVO").list();

        ArrayList<Block> blocks = new ArrayList<>();
        for (BlockVO blockVO : result) {
            blocks.add(new Block(blockVO.getRlp()));
        }

        return blocks;
    }

    @Override
    @Transactional
    public void reset() {
        sessionFactory.getCurrentSession().
                createQuery("delete from BlockVO").executeUpdate();
    }

    @Override
    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {

        List result = sessionFactory.getCurrentSession().
                createQuery("from TransactionReceiptVO where hash = :hash").
                setParameter("hash", hash).list();

        if (result.size() == 0) return null;
        TransactionReceiptVO vo = (TransactionReceiptVO) result.get(0);

        return new TransactionReceipt(vo.rlp);

    }
}

package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.*;
import org.ethereum.datasource.Flushable;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.math.BigInteger.ZERO;
import static org.ethereum.crypto.HashUtil.shortHash;
import static org.spongycastle.util.Arrays.areEqual;

public class IndexedBlockStore extends AbstractBlockstore{

    private static final Logger logger = LoggerFactory.getLogger("general");

    KeyValueDataSource indexDS;
    DataSourceArray<List<BlockInfo>> index;
    KeyValueDataSource blocksDS;
    ObjectDataSource<Block> blocks;

    public static final Serializer<List<BlockInfo>, byte[]> BLOCK_INFO_SERIALIZER = new Serializer<List<BlockInfo>, byte[]>(){

        @Override
        public byte[] serialize(List<BlockInfo> value) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(value);

                byte[] data = bos.toByteArray();
                return data;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<BlockInfo> deserialize(byte[] bytes) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, bytes.length);
                ObjectInputStream ois = new ObjectInputStream(bis);
                return (List<BlockInfo>)ois.readObject();
            } catch (IOException|ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public IndexedBlockStore(){
    }

//    public void init(Map<Long, List<BlockInfo>> longListHashMap, KeyValueDataSource hashMapDB, Object o, Object o1) {
//        throw new RuntimeException("To remove");
//    }

    public void init(KeyValueDataSource index, KeyValueDataSource blocks) {
        indexDS = index;
        this.index = new DataSourceArray<>(
                new ObjectDataSource<>(index, BLOCK_INFO_SERIALIZER).withCacheSize(256));
        this.blocksDS = blocks;
        this.blocks = new ObjectDataSource<>(blocks, new Serializer<Block, byte[]>() {
            @Override
            public byte[] serialize(Block block) {
                return block.getEncoded();
            }

            @Override
            public Block deserialize(byte[] bytes) {
                return new Block(bytes);
            }
        }).withCacheSize(256);
    }

    public Block getBestBlock(){

        Long maxLevel = getMaxNumber();
        if (maxLevel < 0) return null;

        Block bestBlock = getChainBlockByNumber(maxLevel);
        if (bestBlock != null) return  bestBlock;

        // That scenario can happen
        // if there is a fork branch that is
        // higher than main branch but has
        // less TD than the main branch TD
        while (bestBlock == null){
            --maxLevel;
            bestBlock = getChainBlockByNumber(maxLevel);
        }

        return bestBlock;
    }

    public byte[] getBlockHashByNumber(long blockNumber){
        return getChainBlockByNumber(blockNumber).getHash(); // FIXME: can be improved by accessing the hash directly in the index
    }


    @Override
    public void flush(){
        blocks.flush();
        index.flush();
        if (blocksDS instanceof Flushable) {
            ((Flushable)blocksDS).flush();
        }
        if (indexDS instanceof Flushable) {
            ((Flushable)indexDS).flush();
        }
    }


    @Override
    public void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain){
        addInternalBlock(block, cummDifficulty, mainChain);
    }

    private void addInternalBlock(Block block, BigInteger cummDifficulty, boolean mainChain){

        List<BlockInfo> blockInfos = block.getNumber() >= index.size() ?  new ArrayList<BlockInfo>() :
                index.get((int) block.getNumber());

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setCummDifficulty(cummDifficulty);
        blockInfo.setHash(block.getHash());
        blockInfo.setMainChain(mainChain); // FIXME:maybe here I should force reset main chain for all uncles on that level

        blockInfos.add(blockInfo);
        index.set((int) block.getNumber(), blockInfos);

        blocks.put(block.getHash(), block);
    }


    public List<Block> getBlocksByNumber(long number){

        List<Block> result = new ArrayList<>();

        if (number >= index.size()) {
            return result;
        }

        List<BlockInfo> blockInfos = index.get((int) number);

        for (BlockInfo blockInfo : blockInfos){

            byte[] hash = blockInfo.getHash();
            Block block = blocks.get(hash);

            result.add(block);
        }
        return result;
    }

    @Override
    public Block getChainBlockByNumber(long number){
        if (number >= index.size()){
            return null;
        }

        List<BlockInfo> blockInfos = index.get((int) number);

        for (BlockInfo blockInfo : blockInfos){

            if (blockInfo.isMainChain()){

                byte[] hash = blockInfo.getHash();
                return blocks.get(hash);
            }
        }

        return null;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return blocks.get(hash);
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return blocks.get(hash) != null;
    }


    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash){
        Block block = this.getBlockByHash(hash);
        if (block == null) return ZERO;

        Long level  =  block.getNumber();
        List<BlockInfo> blockInfos =  index.get(level.intValue());
        for (BlockInfo blockInfo : blockInfos)
                 if (areEqual(blockInfo.getHash(), hash)) {
                     return blockInfo.cummDifficulty;
                 }

        return ZERO;
    }


        @Override
    public BigInteger getTotalDifficulty(){
        long maxNumber = getMaxNumber();

        List<BlockInfo> blockInfos = index.get((int) maxNumber);
        for (BlockInfo blockInfo : blockInfos){
            if (blockInfo.isMainChain()){
                return blockInfo.getCummDifficulty();
            }
        }

        while (true){
            --maxNumber;
            List<BlockInfo> infos = getBlockInfoForLevel(maxNumber);

            for (BlockInfo blockInfo : infos) {
                if (blockInfo.isMainChain()) {
                    return blockInfo.getCummDifficulty();
                }
            }
        }
    }

    @Override
    public long getMaxNumber(){

        Long bestIndex = 0L;

        if (index.size() > 0){
            bestIndex = (long) index.size();
        }

        return bestIndex - 1L;
    }

    @Override
    public List<byte[]> getListHashesEndWith(byte[] hash, long number){

        List<Block> blocks = getListBlocksEndWith(hash, number);
        List<byte[]> hashes = new ArrayList<>(blocks.size());

        for (Block b : blocks) {
            hashes.add(b.getHash());
        }

        return hashes;
    }

    @Override
    public List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty) {

        List<Block> blocks = getListBlocksEndWith(hash, qty);
        List<BlockHeader> headers = new ArrayList<>(blocks.size());

        for (Block b : blocks) {
            headers.add(b.getHeader());
        }

        return headers;
    }

    @Override
    public List<Block> getListBlocksEndWith(byte[] hash, long qty) {
        return getListBlocksEndWithInner(hash, qty);
    }

    private List<Block> getListBlocksEndWithInner(byte[] hash, long qty) {

        Block block = this.blocks.get(hash);

        if (block == null) return new ArrayList<>();

        List<Block> blocks = new ArrayList<>((int) qty);

        for (int i = 0; i < qty; ++i) {
            blocks.add(block);
            block = this.blocks.get(block.getParentHash());
            if (block == null) break;
        }

        return blocks;
    }

    @Override
    public void reBranch(Block forkBlock){

        Block bestBlock = getBestBlock();

        long maxLevel = Math.max(bestBlock.getNumber(), forkBlock.getNumber());

        // 1. First ensure that you are one the save level
        long currentLevel = maxLevel;
        Block forkLine = forkBlock;
        if (forkBlock.getNumber() > bestBlock.getNumber()){

            while(currentLevel > bestBlock.getNumber()){
                List<BlockInfo> blocks =  getBlockInfoForLevel(currentLevel);
                BlockInfo blockInfo = getBlockInfoForHash(blocks, forkLine.getHash());
                if (blockInfo != null) blockInfo.setMainChain(true);
                forkLine = getBlockByHash(forkLine.getParentHash());
                --currentLevel;
            }
        }

        Block bestLine = bestBlock;
        if (bestBlock.getNumber() > forkBlock.getNumber()){

            while(currentLevel > forkBlock.getNumber()){

                List<BlockInfo> blocks =  getBlockInfoForLevel(currentLevel);
                BlockInfo blockInfo = getBlockInfoForHash(blocks, bestLine.getHash());
                if (blockInfo != null) blockInfo.setMainChain(false);
                bestLine = getBlockByHash(bestLine.getParentHash());
                --currentLevel;
            }
        }


        // 2. Loop back on each level until common block
        while( !bestLine.isEqual(forkLine) ) {

            List<BlockInfo> levelBlocks = getBlockInfoForLevel(currentLevel);
            BlockInfo bestInfo = getBlockInfoForHash(levelBlocks, bestLine.getHash());
            if (bestInfo != null) bestInfo.setMainChain(false);

            BlockInfo forkInfo = getBlockInfoForHash(levelBlocks, forkLine.getHash());
            if (forkInfo != null) forkInfo.setMainChain(true);


            bestLine = getBlockByHash(bestLine.getParentHash());
            forkLine = getBlockByHash(forkLine.getParentHash());

            --currentLevel;
        }


    }


    public List<byte[]> getListHashesStartWith(long number, long maxBlocks){

        List<byte[]> result = new ArrayList<>();

        int i;
        for ( i = 0; i < maxBlocks; ++i){
            List<BlockInfo> blockInfos =  index.get((int) number);
            if (blockInfos == null) break;

            for (BlockInfo blockInfo : blockInfos)
               if (blockInfo.isMainChain()){
                   result.add(blockInfo.getHash());
                   break;
               }

            ++number;
        }
        maxBlocks -= i;

        return result;
    }

    public static class BlockInfo implements Serializable {
        byte[] hash;
        BigInteger cummDifficulty;
        boolean mainChain;

        public byte[] getHash() {
            return hash;
        }

        public void setHash(byte[] hash) {
            this.hash = hash;
        }

        public BigInteger getCummDifficulty() {
            return cummDifficulty;
        }

        public void setCummDifficulty(BigInteger cummDifficulty) {
            this.cummDifficulty = cummDifficulty;
        }

        public boolean isMainChain() {
            return mainChain;
        }

        public void setMainChain(boolean mainChain) {
            this.mainChain = mainChain;
        }
    }

    public void printChain(){

        Long number = getMaxNumber();

        for (int i = 0; i < number; ++i){
            List<BlockInfo> levelInfos = index.get(i);

            if (levelInfos != null) {
                System.out.print(i);
                for (BlockInfo blockInfo : levelInfos){
                    if (blockInfo.isMainChain())
                        System.out.print(" [" + shortHash(blockInfo.getHash()) + "] ");
                    else
                        System.out.print(" " + shortHash(blockInfo.getHash()) + " ");
                }
                System.out.println();
            }

        }

    }

    private List<BlockInfo> getBlockInfoForLevel(long level){
        return index.get((int) level);
    }

    private static BlockInfo getBlockInfoForHash(List<BlockInfo> blocks, byte[] hash){

        for (BlockInfo blockInfo : blocks)
            if (areEqual(hash, blockInfo.getHash())) return blockInfo;

        return null;
    }

    @Override
    public void load() {
    }

    public void setSessionFactory(SessionFactory sessionFactory){
        throw new UnsupportedOperationException();
    }

}

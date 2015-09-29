package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.datasource.KeyValueDataSource;
import org.hibernate.SessionFactory;
import org.mapdb.DB;
import org.mapdb.DataIO;
import org.mapdb.Serializer;
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

public class IndexedBlockStore implements BlockStore{

    private static final Logger logger = LoggerFactory.getLogger("general");

    IndexedBlockStore cache;
    Map<Long, List<BlockInfo>> index;
    KeyValueDataSource blocks;

    DB indexDB;

    public IndexedBlockStore(){
    }

    public void init(Map<Long, List<BlockInfo>> index, KeyValueDataSource blocks, IndexedBlockStore cache, DB indexDB) {
        this.cache = cache;
        this.index = index;
        this.blocks = blocks;
        this.indexDB  = indexDB;
    }

    public Block getBestBlock(){

        Long maxLevel = getMaxNumber();
        if (maxLevel < 0) return null;

        Block bestBlock = getChainBlockByNumber(maxLevel);
        if (bestBlock != null) return  bestBlock;

        // That can scenario can happen
        // if there is fork branch that is
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

        if (cache == null) return;

        long t1 = System.nanoTime();

        for (byte[] hash : cache.blocks.keys()){
            blocks.put(hash, cache.blocks.get(hash));
        }

        for (Map.Entry<Long, List<BlockInfo>> e : cache.index.entrySet()) {
            Long number = e.getKey();
            List<BlockInfo> infos = e.getValue();

            if (index.containsKey(number)) infos.addAll(index.get(number));
            index.put(number, infos);
        }

        cache.blocks.close();
        cache.index.clear();

        long t2 = System.nanoTime();

        if (indexDB != null)
            indexDB.commit();

        logger.info("Flush block store in: {} ms", ((float)(t2 - t1) / 1_000_000));

    }


    @Override
    public void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain){
        if (cache == null)
            addInternalBlock(block, cummDifficulty, mainChain);
        else
            cache.saveBlock(block, cummDifficulty, mainChain);
    }

    private void addInternalBlock(Block block, BigInteger cummDifficulty, boolean mainChain){

        List<BlockInfo> blockInfos = index.get(block.getNumber());
        if (blockInfos == null){
            blockInfos = new ArrayList<>();
        }

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setCummDifficulty(cummDifficulty);
        blockInfo.setHash(block.getHash());
        blockInfo.setMainChain(mainChain); // FIXME:maybe here I should force reset main chain for all uncles on that level

        blockInfos.add(blockInfo);
        index.put(block.getNumber(), blockInfos);

        blocks.put(block.getHash(), block.getEncoded());
    }


    public List<Block> getBlocksByNumber(long number){

        List<Block> result = new ArrayList<>();
        if (cache != null)
            result = cache.getBlocksByNumber(number);

        List<BlockInfo> blockInfos = index.get(number);
        if (blockInfos == null){
            return result;
        }

        for (BlockInfo blockInfo : blockInfos){

            byte[] hash = blockInfo.getHash();
            byte[] blockRlp = blocks.get(hash);

            result.add(new Block(blockRlp));
        }
        return result;
    }

    @Override
    public Block getChainBlockByNumber(long number){

        if (cache != null) {
            Block block = cache.getChainBlockByNumber(number);
            if (block != null) return block;
        }

        List<BlockInfo> blockInfos = index.get(number);
        if (blockInfos == null){
            return null;
        }

        for (BlockInfo blockInfo : blockInfos){

            if (blockInfo.isMainChain()){

                byte[] hash = blockInfo.getHash();
                byte[] blockRlp = blocks.get(hash);
                return new Block(blockRlp);
            }
        }

        return null;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {

        if (cache != null) {
            Block cachedBlock = cache.getBlockByHash(hash);
            if (cachedBlock != null) return cachedBlock;
        }

        byte[] blockRlp = blocks.get(hash);
        if (blockRlp == null)
            return null;

        return new Block(blockRlp);
    }

    @Override
    public boolean isBlockExist(byte[] hash) {

        if (cache != null) {
            Block cachedBlock = cache.getBlockByHash(hash);
            if (cachedBlock != null) return true;
        }

        byte[] blockRlp = blocks.get(hash);
        return blockRlp != null;
    }


    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash){

        if (cache != null && cache.getBlockByHash(hash) != null) {

            return cache.getTotalDifficultyForHash(hash);
        }

        Block block = this.getBlockByHash(hash);
        if (block == null) return ZERO;

        Long level  =  block.getNumber();
        List<BlockInfo> blockInfos =  index.get(level);
        for (BlockInfo blockInfo : blockInfos)
                 if (areEqual(blockInfo.getHash(), hash)) {
                     return blockInfo.cummDifficulty;
                 }

        return ZERO;
    }


        @Override
    public BigInteger getTotalDifficulty(){

        BigInteger cacheTotalDifficulty = ZERO;

        long maxNumber = getMaxNumber();
        if (cache != null) {

            List<BlockInfo> infos = getBlockInfoForLevel(maxNumber);

            if (infos != null){
                for (BlockInfo blockInfo : infos){
                    if (blockInfo.isMainChain()){
                        return blockInfo.getCummDifficulty();
                    }
                }

                // todo: need better testing for that place
                // here is the place when you know
                // for sure that the potential fork
                // branch is higher than main branch
                // in that case the correct td is the
                // first level when you have [mainchain = true] Blockinfo
                boolean found = false;
                Map<Long, List<BlockInfo>> searching = cache.index;
                while (!found){

                    --maxNumber;
                    infos = getBlockInfoForLevel(maxNumber);

                    for (BlockInfo blockInfo : infos) {
                        if (blockInfo.isMainChain()) {
                            found = true;
                            return blockInfo.getCummDifficulty();
                        }
                    }
                }
            }
        }

        List<BlockInfo> blockInfos = index.get(maxNumber);
        for (BlockInfo blockInfo : blockInfos){
            if (blockInfo.isMainChain()){
                return blockInfo.getCummDifficulty();
            }
        }

        return cacheTotalDifficulty;
    }

    @Override
    public long getMaxNumber(){

        Long bestIndex = 0L;

        if (index.size() > 0){
            bestIndex = (long) index.size();
        }

        if (cache != null){
            return bestIndex + cache.index.size() - 1L;
        } else
            return bestIndex - 1L;
    }

    @Override
    public List<byte[]> getListHashesEndWith(byte[] hash, long number){

        List<byte[]> cachedHashes = new ArrayList<>();
        if (cache != null)
           cachedHashes = cache.getListHashesEndWith(hash, number);

        byte[] rlp = blocks.get(hash);
        if (rlp == null) return cachedHashes;

        for (int i = 0; i < number; ++i){

            Block block = new Block(rlp);
            cachedHashes.add(block.getHash());
            rlp = blocks.get(block.getParentHash());
            if (rlp == null) break;
        }

        return cachedHashes;
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
            List<BlockInfo> blockInfos =  index.get(number);
            if (blockInfos == null) break;

            for (BlockInfo blockInfo : blockInfos)
               if (blockInfo.isMainChain()){
                   result.add(blockInfo.getHash());
                   break;
               }

            ++number;
        }
        maxBlocks -= i;

        if (cache != null)
            result.addAll( cache.getListHashesStartWith(number, maxBlocks) );

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


    public static final Serializer<List<BlockInfo>> BLOCK_INFO_SERIALIZER = new Serializer<List<BlockInfo>>(){

        @Override
        public void serialize(DataOutput out, List<BlockInfo> value) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);

            byte[] data = bos.toByteArray();
            DataIO.packInt(out, data.length);
            out.write(data);
        }

        @Override
        public List<BlockInfo> deserialize(DataInput in, int available) throws IOException {

            List<BlockInfo> value = null;
            try {
                int size = DataIO.unpackInt(in);
                byte[] data = new byte[size];
                in.readFully(data);

                ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, data.length);
                ObjectInputStream ois = new ObjectInputStream(bis);
                value = (List<BlockInfo>)ois.readObject();

            } catch (ClassNotFoundException e) {e.printStackTrace();}

            return value;
        }
    };


    public void printChain(){

        Long number = getMaxNumber();

        for (long i = 0; i < number; ++i){
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

        if (cache != null)
            cache.printChain();
    }

    private List<BlockInfo> getBlockInfoForLevel(Long level){

        if (cache != null){
            List<BlockInfo> infos =  cache.index.get(level);
            if (infos != null) return infos;
        }

        return index.get(level);
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

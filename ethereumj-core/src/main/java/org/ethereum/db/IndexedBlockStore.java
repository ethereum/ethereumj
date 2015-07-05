package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.datasource.KeyValueDataSource;
import org.mapdb.DataIO;
import org.mapdb.Serializer;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexedBlockStore {

    IndexedBlockStore cache;
    Map<Long, List<BlockInfo>> index;
    KeyValueDataSource blocks;

    public IndexedBlockStore(){
    }

    public void init(Map<Long, List<BlockInfo>> index, KeyValueDataSource blocks, IndexedBlockStore cache) {
        this.cache = cache;
        this.index = index;
        this.blocks = blocks;
    }

    public void flush(){

        for (byte[] hash : cache.blocks.keys()){
            blocks.put(hash, cache.blocks.get(hash));
        }

        index.putAll( cache.index );

        cache.blocks.close();
        cache.index.clear();
    }


    public void addBlock(Block block, BigInteger cummDifficulty, boolean mainChain){
        if (cache == null)
            addInternalBlock(block, cummDifficulty, mainChain);
        else
            cache.addBlock(block, cummDifficulty, mainChain);
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

    public BigInteger getTotalDifficulty(){

        BigInteger cacheTotalDifficulty = BigInteger.ZERO;

        long maxNumber = getMaxNumber();
        if (cache != null) {
            List<BlockInfo> infos =  cache.index.get(maxNumber);
            if (infos != null){
                for (BlockInfo blockInfo : infos){
                    if (blockInfo.isMainChain()){
                        return blockInfo.getCummDifficulty();
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

    public List<byte[]> getNHashesEndWith(byte[] hash, long number){

        List<byte[]> cachedHashes = new ArrayList<>();
        if (cache != null)
           cachedHashes = cache.getNHashesEndWith(hash, number);

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

    public List<byte[]> getNHashesStartWith(long number, long maxBlocks){


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
            result.addAll( cache.getNHashesStartWith(number, maxBlocks) );

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
}

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
package org.ethereum.db;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.DataSourceArray;
import org.ethereum.datasource.ObjectDataSource;
import org.ethereum.datasource.Serializer;
import org.ethereum.datasource.Source;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigInteger.ZERO;
import static org.ethereum.crypto.HashUtil.shortHash;
import static org.spongycastle.util.Arrays.areEqual;

public class IndexedBlockStore extends AbstractBlockstore{

    private static final Logger logger = LoggerFactory.getLogger("general");

    Source<byte[], byte[]> indexDS;
    DataSourceArray<List<BlockInfo>> index;
    Source<byte[], byte[]> blocksDS;
    ObjectDataSource<Block> blocks;

    public IndexedBlockStore(){
    }

    public void init(Source<byte[], byte[]> index, Source<byte[], byte[]> blocks) {
        indexDS = index;
        this.index = new DataSourceArray<>(
                new ObjectDataSource<>(index, BLOCK_INFO_SERIALIZER, 512));
        this.blocksDS = blocks;
        this.blocks = new ObjectDataSource<>(blocks, new Serializer<Block, byte[]>() {
            @Override
            public byte[] serialize(Block block) {
                return block.getEncoded();
            }

            @Override
            public Block deserialize(byte[] bytes) {
                return bytes == null ? null : new Block(bytes);
            }
        }, 512);
    }

    public synchronized Block getBestBlock(){

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

    public synchronized byte[] getBlockHashByNumber(long blockNumber){
        Block chainBlock = getChainBlockByNumber(blockNumber);
        return chainBlock == null ? null : chainBlock.getHash(); // FIXME: can be improved by accessing the hash directly in the index
    }


    @Override
    public synchronized void flush(){
        blocks.flush();
        index.flush();
        blocksDS.flush();
        indexDS.flush();
    }


    @Override
    public synchronized void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain){
        addInternalBlock(block, cummDifficulty, mainChain);
    }

    private void addInternalBlock(Block block, BigInteger cummDifficulty, boolean mainChain){

        List<BlockInfo> blockInfos = block.getNumber() >= index.size() ?  null : index.get((int) block.getNumber());
        blockInfos = blockInfos == null ? new ArrayList<BlockInfo>() : blockInfos;

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setCummDifficulty(cummDifficulty);
        blockInfo.setHash(block.getHash());
        blockInfo.setMainChain(mainChain); // FIXME:maybe here I should force reset main chain for all uncles on that level

        putBlockInfo(blockInfos, blockInfo);
        index.set((int) block.getNumber(), blockInfos);

        blocks.put(block.getHash(), block);
    }

    private void putBlockInfo(List<BlockInfo> blockInfos, BlockInfo blockInfo) {
        for (int i = 0; i < blockInfos.size(); i++) {
            BlockInfo curBlockInfo = blockInfos.get(i);
            if (FastByteComparisons.equal(curBlockInfo.getHash(), blockInfo.getHash())) {
                blockInfos.set(i, blockInfo);
                return;
            }
        }
        blockInfos.add(blockInfo);
    }


    public synchronized List<Block> getBlocksByNumber(long number){

        List<Block> result = new ArrayList<>();

        if (number >= index.size()) {
            return result;
        }

        List<BlockInfo> blockInfos = index.get((int) number);

        if (blockInfos == null) {
            return result;
        }

        for (BlockInfo blockInfo : blockInfos){

            byte[] hash = blockInfo.getHash();
            Block block = blocks.get(hash);

            result.add(block);
        }
        return result;
    }

    @Override
    public synchronized Block getChainBlockByNumber(long number){
        if (number >= index.size()){
            return null;
        }

        List<BlockInfo> blockInfos = index.get((int) number);

        if (blockInfos == null) {
            return null;
        }

        for (BlockInfo blockInfo : blockInfos){

            if (blockInfo.isMainChain()){

                byte[] hash = blockInfo.getHash();
                return blocks.get(hash);
            }
        }

        return null;
    }

    @Override
    public synchronized Block getBlockByHash(byte[] hash) {
        return blocks.get(hash);
    }

    @Override
    public synchronized boolean isBlockExist(byte[] hash) {
        return blocks.get(hash) != null;
    }


    @Override
    public synchronized BigInteger getTotalDifficultyForHash(byte[] hash){
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
    public synchronized BigInteger getTotalDifficulty(){
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

    public synchronized void updateTotDifficulties(long index) {
        List<BlockInfo> level = getBlockInfoForLevel(index);
        for (BlockInfo blockInfo : level) {
            Block block = getBlockByHash(blockInfo.getHash());
            List<BlockInfo> parentInfos = getBlockInfoForLevel(index - 1);
            BlockInfo parentInfo = getBlockInfoForHash(parentInfos, block.getParentHash());
            blockInfo.setCummDifficulty(parentInfo.getCummDifficulty().add(block.getDifficultyBI()));
        }
        this.index.set((int) index, level);
    }

    @Override
    public synchronized long getMaxNumber(){

        Long bestIndex = 0L;

        if (index.size() > 0){
            bestIndex = (long) index.size();
        }

        return bestIndex - 1L;
    }

    @Override
    public synchronized List<byte[]> getListHashesEndWith(byte[] hash, long number){

        List<Block> blocks = getListBlocksEndWith(hash, number);
        List<byte[]> hashes = new ArrayList<>(blocks.size());

        for (Block b : blocks) {
            hashes.add(b.getHash());
        }

        return hashes;
    }

    @Override
    public synchronized List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty) {

        List<Block> blocks = getListBlocksEndWith(hash, qty);
        List<BlockHeader> headers = new ArrayList<>(blocks.size());

        for (Block b : blocks) {
            headers.add(b.getHeader());
        }

        return headers;
    }

    @Override
    public synchronized List<Block> getListBlocksEndWith(byte[] hash, long qty) {
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
    public synchronized void reBranch(Block forkBlock){

        Block bestBlock = getBestBlock();

        long maxLevel = Math.max(bestBlock.getNumber(), forkBlock.getNumber());

        // 1. First ensure that you are one the save level
        long currentLevel = maxLevel;
        Block forkLine = forkBlock;
        if (forkBlock.getNumber() > bestBlock.getNumber()){

            while(currentLevel > bestBlock.getNumber()){
                List<BlockInfo> blocks =  getBlockInfoForLevel(currentLevel);
                BlockInfo blockInfo = getBlockInfoForHash(blocks, forkLine.getHash());
                if (blockInfo != null)  {
                    blockInfo.setMainChain(true);
                    setBlockInfoForLevel(currentLevel, blocks);
                }
                forkLine = getBlockByHash(forkLine.getParentHash());
                --currentLevel;
            }
        }

        Block bestLine = bestBlock;
        if (bestBlock.getNumber() > forkBlock.getNumber()){

            while(currentLevel > forkBlock.getNumber()){

                List<BlockInfo> blocks =  getBlockInfoForLevel(currentLevel);
                BlockInfo blockInfo = getBlockInfoForHash(blocks, bestLine.getHash());
                if (blockInfo != null)  {
                    blockInfo.setMainChain(false);
                    setBlockInfoForLevel(currentLevel, blocks);
                }
                bestLine = getBlockByHash(bestLine.getParentHash());
                --currentLevel;
            }
        }


        // 2. Loop back on each level until common block
        while( !bestLine.isEqual(forkLine) ) {

            List<BlockInfo> levelBlocks = getBlockInfoForLevel(currentLevel);
            BlockInfo bestInfo = getBlockInfoForHash(levelBlocks, bestLine.getHash());
            if (bestInfo != null) {
                bestInfo.setMainChain(false);
                setBlockInfoForLevel(currentLevel, levelBlocks);
            }

            BlockInfo forkInfo = getBlockInfoForHash(levelBlocks, forkLine.getHash());
            if (forkInfo != null) {
                forkInfo.setMainChain(true);
                setBlockInfoForLevel(currentLevel, levelBlocks);
            }


            bestLine = getBlockByHash(bestLine.getParentHash());
            forkLine = getBlockByHash(forkLine.getParentHash());

            --currentLevel;
        }


    }


    public synchronized List<byte[]> getListHashesStartWith(long number, long maxBlocks){

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


    public static final Serializer<List<BlockInfo>, byte[]> BLOCK_INFO_SERIALIZER = new Serializer<List<BlockInfo>, byte[]>(){

        @Override
        public byte[] serialize(List<BlockInfo> value) {
                List<byte[]> rlpBlockInfoList = new ArrayList<>();
                for (BlockInfo blockInfo : value) {
                    byte[] hash = RLP.encodeElement(blockInfo.getHash());
                    // Encoding works correctly only with positive BigIntegers
                    if (blockInfo.getCummDifficulty() == null || blockInfo.getCummDifficulty().compareTo(BigInteger.ZERO) < 0) {
                        throw new RuntimeException("BlockInfo cummDifficulty should be positive BigInteger");
                    }
                    byte[] cummDiff = RLP.encodeBigInteger(blockInfo.getCummDifficulty());
                    byte[] isMainChain = RLP.encodeInt(blockInfo.isMainChain() ? 1 : 0);
                    rlpBlockInfoList.add(RLP.encodeList(hash, cummDiff, isMainChain));
                }
                byte[][] elements = rlpBlockInfoList.toArray(new byte[rlpBlockInfoList.size()][]);

                return RLP.encodeList(elements);
        }

        @Override
        public List<BlockInfo> deserialize(byte[] bytes) {
            if (bytes == null) return null;

            List<BlockInfo> blockInfoList = new ArrayList<>();
            RLPList list = (RLPList) RLP.decode2(bytes).get(0);
            for (RLPElement element : list) {
                RLPList rlpBlock = (RLPList) element;
                BlockInfo blockInfo = new BlockInfo();
                byte[] rlpHash = rlpBlock.get(0).getRLPData();
                blockInfo.setHash(rlpHash == null ? new byte[0] : rlpHash);
                byte[] rlpCummDiff = rlpBlock.get(1).getRLPData();
                blockInfo.setCummDifficulty(rlpCummDiff == null ? BigInteger.ZERO : ByteUtil.bytesToBigInteger(rlpCummDiff));
                blockInfo.setMainChain(ByteUtil.byteArrayToInt(rlpBlock.get(2).getRLPData()) == 1);
                blockInfoList.add(blockInfo);
            }

            return blockInfoList;
        }
    };


    public synchronized void printChain(){

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

    private synchronized List<BlockInfo> getBlockInfoForLevel(long level){
        return index.get((int) level);
    }

    private synchronized void setBlockInfoForLevel(long level, List<BlockInfo> infos){
        index.set((int) level, infos);
    }

    private static BlockInfo getBlockInfoForHash(List<BlockInfo> blocks, byte[] hash){

        for (BlockInfo blockInfo : blocks)
            if (areEqual(hash, blockInfo.getHash())) return blockInfo;

        return null;
    }

    @Override
    public synchronized void load() {
    }

    @Override
    public synchronized void close() {
//        logger.info("Closing IndexedBlockStore...");
//        try {
//            indexDS.close();
//        } catch (Exception e) {
//            logger.warn("Problems closing indexDS", e);
//        }
//        try {
//            blocksDS.close();
//        } catch (Exception e) {
//            logger.warn("Problems closing blocksDS", e);
//        }
    }
}

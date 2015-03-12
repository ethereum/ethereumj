package org.ethereum.jsontestsuite;

import java.util.Arrays;
import java.util.Map;

public class BlockTestCase {

    private Block[] blocks;
    private BlockHeader genesisBlockHeader;
    private Map<String, AccountState2> pre;


    public BlockTestCase() {
    }

    public Block[] getBlocks() {
        return blocks;
    }

    public void setBlocks(Block[] blocks) {
        this.blocks = blocks;
    }

    public BlockHeader getGenesisBlockHeader() {
        return genesisBlockHeader;
    }

    public void setGenesisBlockHeader(BlockHeader genesisBlockHeader) {
        this.genesisBlockHeader = genesisBlockHeader;
    }

    public Map<String, AccountState2> getPre() {
        return pre;
    }

    public void setPre(Map<String, AccountState2> pre) {
        this.pre = pre;
    }

    @Override
    public String toString() {
        return "BlockTestCase{" +
                "blocks=" + Arrays.toString(blocks) +
                ", genesisBlockHeader=" + genesisBlockHeader +
                ", pre=" + pre +
                '}';
    }
}

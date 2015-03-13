package org.ethereum.jsontestsuite;

import org.ethereum.jsontestsuite.model.AccountTCK;
import org.ethereum.jsontestsuite.model.BlockHeaderTck;
import org.ethereum.jsontestsuite.model.BlockTck;

import java.util.List;
import java.util.Map;

public class BlockTestCase {

    private List<BlockTck> blocks;
    private BlockHeaderTck genesisBlockHeader;
    private Map<String, AccountTCK> pre;


    public BlockTestCase() {
    }

    public List<BlockTck> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockTck> blocks) {
        this.blocks = blocks;
    }

    public BlockHeaderTck getGenesisBlockHeader() {
        return genesisBlockHeader;
    }

    public void setGenesisBlockHeader(BlockHeaderTck genesisBlockHeader) {
        this.genesisBlockHeader = genesisBlockHeader;
    }

    public Map<String, AccountTCK> getPre() {
        return pre;
    }

    public void setPre(Map<String, AccountTCK> pre) {
        this.pre = pre;
    }

    @Override
    public String toString() {
        return "BlockTestCase{" +
                "blocks=" + blocks +
                ", genesisBlockHeader=" + genesisBlockHeader +
                ", pre=" + pre +
                '}';
    }
}

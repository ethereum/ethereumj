package org.ethereum.jsontestsuite.suite.builder;

import org.ethereum.core.BlockHeader;
import org.ethereum.jsontestsuite.suite.Utils;
import org.ethereum.jsontestsuite.suite.model.BlockHeaderTck;

import java.math.BigInteger;

public class BlockHeaderBuilder {


    public static BlockHeader  build(BlockHeaderTck headerTck){

        BlockHeader header = new BlockHeader(
                Utils.parseData(headerTck.getParentHash()),
                Utils.parseData(headerTck.getUncleHash()),
                Utils.parseData(headerTck.getCoinbase()),
                Utils.parseData(headerTck.getBloom()),
                Utils.parseNumericData(headerTck.getDifficulty()),
                new BigInteger(1, Utils.parseData(headerTck.getNumber())).longValue(),
                Utils.parseData(headerTck.getGasLimit()),
                new BigInteger(1, Utils.parseData(headerTck.getGasUsed())).longValue(),
                new BigInteger(1, Utils.parseData(headerTck.getTimestamp())).longValue(),
                Utils.parseData(headerTck.getExtraData()),
                Utils.parseData(headerTck.getMixHash()),
                Utils.parseData(headerTck.getNonce())
        );

        header.setReceiptsRoot(Utils.parseData(headerTck.getReceiptTrie()));
        header.setTransactionsRoot(Utils.parseData(headerTck.getTransactionsTrie()));
        header.setStateRoot(Utils.parseData(headerTck.getStateRoot()));

        return header;
    }

}

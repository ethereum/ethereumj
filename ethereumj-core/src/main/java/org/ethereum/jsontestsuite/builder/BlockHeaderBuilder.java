package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.jsontestsuite.Utils;
import org.ethereum.jsontestsuite.model.BlockHeaderTck;

import java.math.BigInteger;

import static org.ethereum.jsontestsuite.Utils.parseData;
import static org.ethereum.jsontestsuite.Utils.parseNumericData;

public class BlockHeaderBuilder {


    public static BlockHeader  build(BlockHeaderTck headerTck){

        BlockHeader header = new BlockHeader(
                parseData(headerTck.getParentHash()),
                parseData(headerTck.getUncleHash()),
                parseData(headerTck.getCoinbase()),
                parseData(headerTck.getBloom()),
                parseNumericData(headerTck.getDifficulty()),
                new BigInteger(1, parseData(headerTck.getNumber())).longValue(),
                parseData(headerTck.getGasLimit()),
                new BigInteger(1, parseData(headerTck.getGasUsed())).longValue(),
                new BigInteger(1, parseData(headerTck.getTimestamp())).longValue(),
                parseData(headerTck.getExtraData()),
                parseData(headerTck.getMixHash()),
                parseData(headerTck.getNonce())
        );

        header.setReceiptsRoot(parseData(headerTck.getReceiptTrie()));
        header.setTransactionsRoot(parseData(headerTck.getTransactionsTrie()));
        header.setStateRoot(parseData(headerTck.getStateRoot()));

        return header;
    }

}

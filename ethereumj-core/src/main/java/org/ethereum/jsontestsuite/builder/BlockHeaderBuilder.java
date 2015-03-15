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
                new BigInteger(headerTck.getNumber()).longValue(),
                new BigInteger(headerTck.getGasLimit()).longValue(),
                new BigInteger(headerTck.getGasUsed()).longValue(),
                new BigInteger(headerTck.getTimestamp()).longValue(),
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

package org.ethereum.jsontestsuite.builder;

import org.ethereum.core.Block;
import org.ethereum.jsontestsuite.model.BlockHeaderTck;
import org.ethereum.jsontestsuite.model.TransactionTck;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.jsontestsuite.Utils.parseData;

public class BlockBuilder {


    public static Block build(BlockHeaderTck header,
                              List<TransactionTck> transactions,
                              List<BlockHeaderTck> uncles) {

        Block block = new Block(
                parseData(header.getParentHash()),
                parseData(header.getUncleHash()),
                parseData(header.getCoinbase()),
                parseData(header.getBloom()),
                parseData(header.getDifficulty()),
                new BigInteger(header.getNumber()).longValue(),
                new BigInteger(header.getGasLimit()).longValue(),
                new BigInteger(header.getGasUsed()).longValue(),
                new BigInteger(header.getTimestamp()).longValue(),
                parseData(header.getSeedHash()),
                parseData(header.getMixHash()),
                parseData(header.getExtraData()),
                parseData(header.getNonce()), null, null);

        // TODO: transactions
        // TODO: uncles


        return block;
    }
}

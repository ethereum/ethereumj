package org.ethereum.jsontestsuite.suite.builder;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.jsontestsuite.suite.Env;
import org.ethereum.jsontestsuite.suite.model.BlockHeaderTck;
import org.ethereum.jsontestsuite.suite.model.TransactionTck;
import org.ethereum.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.util.BIUtil.toBI;
import static org.ethereum.util.ByteUtil.byteArrayToLong;

public class BlockBuilder {


    public static Block build(BlockHeaderTck header,
                              List<TransactionTck> transactionsTck,
                              List<BlockHeaderTck> unclesTck) {

        if (header == null) return null;

        List<BlockHeader> uncles = new ArrayList<>();
        if (unclesTck != null) for (BlockHeaderTck uncle : unclesTck)
            uncles.add(BlockHeaderBuilder.build(uncle));

        List<Transaction> transactions = new ArrayList<>();
        if (transactionsTck != null) for (TransactionTck tx : transactionsTck)
            transactions.add(TransactionBuilder.build(tx));

        BlockHeader blockHeader = BlockHeaderBuilder.build(header);
        Block block = new Block(
                blockHeader,
                transactions, uncles);

        return block;
    }


    public static Block build(Env env){

        Block block = new Block(
                ByteUtil.EMPTY_BYTE_ARRAY,
                ByteUtil.EMPTY_BYTE_ARRAY,
                env.getCurrentCoinbase(),
                ByteUtil.EMPTY_BYTE_ARRAY,
                env.getCurrentDifficulty(),

                byteArrayToLong(env.getCurrentNumber()),
                env.getCurrentGasLimit(),
                0L,
                byteArrayToLong(env.getCurrentTimestamp()),
                new byte[32],
                ByteUtil.ZERO_BYTE_ARRAY,
                ByteUtil.ZERO_BYTE_ARRAY,
                null, null);

        return block;
    }
}

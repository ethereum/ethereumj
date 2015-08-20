package org.ethereum.net.eth.handler;

import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.net.eth.message.*;
import org.ethereum.util.ByteUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.eth.EthVersion.*;

/**
 * Eth V61
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Component
@Scope("prototype")
public class Eth61 extends EthHandler {

    public Eth61() {
        super(V61);
    }

    @Override
    protected void sendStatus() {
        byte protocolVersion = version.getCode(), networkId = (byte) CONFIG.networkId();
        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = blockchain.getBestBlockHash();
        StatusMessage msg = new StatusMessage61(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
        sendMessage(msg);
    }

    @Override
    protected void processStatus(StatusMessage msg) {
        bestHash = msg.getBestHash();
    }

    @Override
    public void sendGetBlockHashesByNumber(long blockNumber, int maxHashesAsk) {
        GetBlockHashesByNumberMessage msg = new GetBlockHashesByNumberMessage(blockNumber, maxHashesAsk);
        sendMessage(msg);
    }

    @Override
    protected void processGetBlockHashesByNumber(GetBlockHashesByNumberMessage msg) {
        List<byte[]> hashes = blockchain.getListOfHashesStartFromBlock(msg.getBlockNumber(), msg.getMaxBlocks());

        BlockHashesMessage msgHashes = new BlockHashesMessage(hashes);
        sendMessage(msgHashes);
    }
}

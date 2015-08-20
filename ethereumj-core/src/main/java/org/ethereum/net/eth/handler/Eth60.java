package org.ethereum.net.eth.handler;

import org.ethereum.core.Blockchain;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.eth.message.StatusMessage60;
import org.ethereum.util.ByteUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.net.eth.EthVersion.*;

/**
 * Eth V60
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Component
@Scope("prototype")
public class Eth60 extends EthHandler {

    public Eth60() {
        super(V60);
    }

    @Override
    protected void processStatus(StatusMessage msg) {
        bestHash = msg.getBestHash();
    }

    @Override
    protected void sendStatus() {
        byte protocolVersion = version.getCode(), networkId = (byte) CONFIG.networkId();
        BigInteger totalDifficulty = blockchain.getTotalDifficulty();
        byte[] bestHash = blockchain.getBestBlockHash();
        StatusMessage msg = new StatusMessage60(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, Blockchain.GENESIS_HASH);
        sendMessage(msg);
    }
}

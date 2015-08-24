package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.message.GetBlockHashesByNumberMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
    public void sendGetBlockHashesByNumber(long blockNumber, int maxHashesAsk) {
        // not a part of V60
    }

    @Override
    protected void processGetBlockHashesByNumber(GetBlockHashesByNumberMessage msg) {
        // not a part of V60
    }
}

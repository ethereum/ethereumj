package org.ethereum.net.eth;

import org.ethereum.net.eth.message.GetBlockHashesByNumberMessage;
import org.ethereum.net.eth.message.NewBlockHashesMessage;
import org.ethereum.net.rlpx.discover.NodeStatistics;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.ethereum.net.eth.EthVersion.*;

/**
 * Implements Version 60 of Eth protocol
 *
 * @author Mikhail Kalinin
 * @since 18.08.2015
 */
@Component
@Scope("prototype")
public class Eth60 extends Eth {

    public Eth60() {
        super(V60);
    }

    @Override
    void processGetBlockHashesByNumber(GetBlockHashesByNumberMessage msg) {
        // not a part of V60
    }
}

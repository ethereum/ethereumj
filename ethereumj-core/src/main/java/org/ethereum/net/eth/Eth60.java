package org.ethereum.net.eth;

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

    public Eth60(EthHandler handler, NodeStatistics nodeStats) {
        super(V60);

        this.handler = handler;
        this.nodeStats = nodeStats;
    }
}

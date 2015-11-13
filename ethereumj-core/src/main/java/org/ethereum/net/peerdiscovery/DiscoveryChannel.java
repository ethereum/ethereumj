package org.ethereum.net.peerdiscovery;

import io.netty.channel.*;
import org.ethereum.net.ChannelBase;
import org.ethereum.net.eth.handler.EthHandler;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * This class creates the connection to an remote address using the Netty framework
 *
 * @see <a href="http://netty.io">http://netty.io</a>
 */
@Component
@Scope("prototype")
public class DiscoveryChannel extends ChannelBase {

    public DiscoveryChannel() {
        super();
        remoteId = new byte[0];
    }

    @Override
    public void config() {
        super.config();

        channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(32368));
        channel.config().setOption(ChannelOption.SO_RCVBUF, 32368);
        channel.config().setOption(ChannelOption.SO_BACKLOG, 1024);
    }

    public HelloMessage getHelloHandshake() {
        return p2pHandler.getHandshakeHelloMessage();
    }

    public StatusMessage getStatusHandshake() {
        return nodeStatistics.getEthLastInboundStatusMsg();
    }
}

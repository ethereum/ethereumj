/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.eth.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.db.BlockStore;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.message.EthMessage;
import org.ethereum.net.eth.message.EthMessageCodes;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.server.Channel;
import org.ethereum.publish.BackwardCompatibilityEthereumListenerProxy;
import org.ethereum.publish.Publisher;
import org.ethereum.publish.Subscription;
import org.ethereum.publish.event.BlockAdded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * Process the messages between peers with 'eth' capability on the network<br>
 * Contains common logic to all supported versions
 * delegating version specific stuff to its descendants
 */
public abstract class EthHandler extends SimpleChannelInboundHandler<EthMessage> implements Eth {

    private final static Logger logger = LoggerFactory.getLogger("net");

    protected Blockchain blockchain;

    protected SystemProperties config;

    protected EthereumListener listener;

    protected Channel channel;

    private MessageQueue msgQueue = null;

    protected EthVersion version;

    protected boolean peerDiscoveryMode = false;

    protected Block bestBlock;

    protected boolean processTransactions = false;
    private Subscription<BlockAdded, BlockAdded.Data> bestBlockSub;

    protected EthHandler(EthVersion version) {
        this.version = version;
    }

    protected EthHandler(final EthVersion version, final SystemProperties config,
                         final Blockchain blockchain, final BlockStore blockStore,
                         final EthereumListener listener) {
        this.version = version;
        this.config = config;
        this.blockchain = blockchain;
        this.bestBlock = blockStore.getBestBlock();
        this.listener = listener;
        this.bestBlockSub = getPublisher().subscribe(BlockAdded.class, this::setBestBlock);

        // when sync enabled we delay transactions processing until sync is complete
        this.processTransactions = !config.isSyncEnabled();
    }

    private void setBestBlock(BlockAdded.Data data) {
        this.bestBlock = data.getBlockSummary().getBlock();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        if (EthMessageCodes.inRange(msg.getCommand().asByte(), version))
            logger.trace("EthHandler invoke: [{}]", msg.getCommand());

        listener.trace(format("EthHandler invoke: [%s]", msg.getCommand()));

        channel.getNodeStatistics().ethInbound.add();

        msgQueue.receivedMessage(msg);
    }

    public Publisher getPublisher() {
        return ((BackwardCompatibilityEthereumListenerProxy) listener).getPublisher();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Eth handling failed", cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.debug("handlerRemoved: kill timers in EthHandler");
        getPublisher().unsubscribe(bestBlockSub);
        onShutdown();
    }

    public void activate() {
        logger.debug("ETH protocol activated");
        listener.trace("ETH protocol activated");
        sendStatus();
    }

    protected void disconnect(ReasonCode reason) {
        msgQueue.disconnect(reason);
        channel.getNodeStatistics().nodeDisconnectedLocal(reason);
    }

    protected void sendMessage(EthMessage message) {
        msgQueue.sendMessage(message);
        channel.getNodeStatistics().ethOutbound.add();
    }

    public StatusMessage getHandshakeStatusMessage() {
        return channel.getNodeStatistics().getEthLastInboundStatusMsg();
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public EthVersion getVersion() {
        return version;
    }

}
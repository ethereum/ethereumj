package org.ethereum.net.server;

import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import org.ethereum.core.Transaction;
import org.ethereum.net.ChannelBase;
import org.ethereum.net.ProtocolHandler;
import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.message.Message;
import org.ethereum.sync.SyncStateName;
import org.ethereum.sync.SyncStatistics;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;


/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
@Component
@Scope("prototype")
public class Channel extends ChannelBase {


    private int ethInbound;
    private int ethOutbound;

    public Channel() {
        super();
    }

    @Override
    public void config() {
        super.config();
        // limit the size of receiving buffer to 1024
        channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(16_777_216));
        channel.config().setOption(ChannelOption.SO_RCVBUF, 16_777_216);
        channel.config().setOption(ChannelOption.SO_BACKLOG, 1024);
    }

    // ProtocolHandlerListener

    @Override
    public void onProtocolActivated(String protocolName, ProtocolHandler protocolHandler) {
        super.onProtocolActivated(protocolName, protocolHandler);
        switch(protocolName) {
            case Capability.ETH:
                this.ethInbound = nodeStatistics.ethInbound.get();
                this.ethOutbound = nodeStatistics.ethOutbound.get();
                break;
        }
    }

    @Override
    public void onRemoteDisconnect(String protocol, Message message) {
        super.onRemoteDisconnect(protocol, message);
        if (nodeStatistics.ethInbound.get() - ethInbound > 1 ||
                nodeStatistics.ethOutbound.get() - ethOutbound > 1) {

            // it means that we've been disconnected
            // after some incorrect action from our peer
            // need to log this moment
            logger.info("From: \t{}\t [DISCONNECT reason=BAD_PEER_ACTION]", this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Channel channel = (Channel) o;

        if (inetSocketAddress != null ? !inetSocketAddress.equals(channel.inetSocketAddress) : channel.inetSocketAddress != null) return false;
        return !(node != null ? !node.equals(channel.node) : channel.node != null);

    }

    public boolean isProtocolsInitialized() {
        return p2pHandler.getEth().hasStatusPassed();
    }


    // ETH sub protocol

    public boolean isEthCompatible(Channel peer) {
        return peer != null && peer.getEthVersion().isCompatible(getEthVersion());
    }

    public boolean hasEthStatusSucceeded() {
        return p2pHandler.getEth().hasStatusSucceeded();
    }

    public void logSyncStats() {
        p2pHandler.getEth().logSyncStats();
    }

    public BigInteger getTotalDifficulty() {
        return nodeStatistics.getEthTotalDifficulty();
    }

    public void changeSyncState(SyncStateName newState) {
        p2pHandler.getEth().changeState(newState);
    }

    public boolean hasBlocksLack() {
        return p2pHandler.getEth().hasBlocksLack();
    }

    public void setMaxHashesAsk(int maxHashesAsk) {
        p2pHandler.getEth().setMaxHashesAsk(maxHashesAsk);
    }

    public int getMaxHashesAsk() {
        return p2pHandler.getEth().getMaxHashesAsk();
    }

    public void setLastHashToAsk(byte[] lastHashToAsk) {
        p2pHandler.getEth().setLastHashToAsk(lastHashToAsk);
    }

    public byte[] getLastHashToAsk() {
        return p2pHandler.getEth().getLastHashToAsk();
    }

    public byte[] getBestKnownHash() {
        return p2pHandler.getEth().getBestKnownHash();
    }

    public SyncStatistics getSyncStats() {
        return p2pHandler.getEth().getStats();
    }

    public boolean isHashRetrievingDone() {
        return p2pHandler.getEth().isHashRetrievingDone();
    }

    public boolean isHashRetrieving() {
        return p2pHandler.getEth().isHashRetrieving();
    }

    public boolean isIdle() {
        return p2pHandler.getEth().isIdle();
    }

    public void prohibitTransactionProcessing() {
        p2pHandler.getEth().disableTransactions();
    }

    public void sendTransaction(Transaction tx) {
        p2pHandler.getEth().sendTransaction(tx);
    }

    public EthVersion getEthVersion() {
        return p2pHandler.getEth().getVersion();
    }


}

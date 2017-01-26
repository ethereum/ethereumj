package org.ethereum.net.par.handler;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.BlockStore;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.handler.Eth;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.par.ParVersion;
import org.ethereum.net.par.message.ParMessage;
import org.ethereum.net.par.message.ParMessageCodes;
import org.ethereum.net.server.Channel;
import org.ethereum.sync.PeerState;
import org.ethereum.util.RLPElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ethereum.net.message.ReasonCode.USELESS_PEER;
import static org.ethereum.sync.PeerState.IDLE;

/**
 * Process the messages between peers with 'par' capability on the network<br>
 * Contains common logic to all supported versions
 * delegating version specific stuff to its descendants
 *
 */
public abstract class ParHandler extends SimpleChannelInboundHandler<ParMessage> {

    private final static Logger logger = LoggerFactory.getLogger("net");

    protected PeerState peerState = IDLE;

    protected long lastReqSentTime;

    protected Blockchain blockchain;

    protected SystemProperties config;

    protected CompositeEthereumListener ethereumListener;

    protected Channel channel;

    private MessageQueue msgQueue = null;

    protected ParVersion version;

    protected boolean peerDiscoveryMode = false;

    protected Eth ethHandler;

    protected Block bestBlock;

    protected boolean statusPassed = false;

    protected SnapshotManifest snapshotManifest;

    protected EthereumListener listener = new EthereumListenerAdapter() {
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            bestBlock = block;
        }
    };

    protected ParHandler(ParVersion version) {
        this.version = version;
    }

    protected ParHandler(final ParVersion version, final SystemProperties config,
                         final Blockchain blockchain, final BlockStore blockStore,
                         final CompositeEthereumListener ethereumListener) {
        this.version = version;
        this.config = config;
        this.ethereumListener = ethereumListener;
        this.blockchain = blockchain;
        bestBlock = blockStore.getBestBlock();
        this.ethereumListener.addListener(listener);
        // when sync enabled we delay transactions processing until sync is complete
//        processTransactions = !config.isSyncEnabled();
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ParMessage msg) throws InterruptedException {


        if (ParMessageCodes.inRange(msg.getCommand().asByte(), version))
            logger.trace("ParHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("ParHandler invoke: [%s]", msg.getCommand()));
        // FIXME: Par instead of eth
//        channel.getNodeStatistics().ethInbound.add();

        msgQueue.receivedMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Par handling failed", cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.debug("handlerRemoved: kill timers in ParHandler");
        ethereumListener.removeListener(listener);
    }

    public void activate() {
        logger.debug("PAR protocol activated");
        ethereumListener.trace("PAR protocol activated");
    }

    protected void disconnect(ReasonCode reason) {
        msgQueue.disconnect(reason);
        channel.getNodeStatistics().nodeDisconnectedLocal(reason);
    }

    protected void sendMessage(ParMessage message) {
        msgQueue.sendMessage(message);
        // FIXME: Par instead of eth
        channel.getNodeStatistics().ethOutbound.add();
    }

    public StatusMessage getHandshakeStatusMessage() {
        // FIXME: Par instead of eth
        return channel.getNodeStatistics().getEthLastInboundStatusMsg();
    }

    public synchronized ListenableFuture<SnapshotManifest> requestManifest() {
        return null;
    }

    public synchronized ListenableFuture<RLPElement> requestSnapshotData(byte[] snapshotHash) {
        return null;
    }

    public boolean isIdle() {
        return peerState == IDLE;
    }

    // TODO: Add something like Eth.hasStatusPassed

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }

    /**
     * @return true if StatusMessage was processed, false otherwise
     */
    public boolean hasStatusPassed() {
        return statusPassed;
    };

    public SnapshotManifest getShortManifest() {
        return snapshotManifest;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setEthHandler(Eth ethHandler) {
        this.ethHandler = ethHandler;
    }

    public ParVersion getVersion() {
        return version;
    }

    public synchronized void dropConnection() {
        logger.info("Peer {}: is a bad one, drop", channel.getPeerIdShort());
        disconnect(USELESS_PEER);
    }

    public String getSyncStats() {
        int waitResp = lastReqSentTime > 0 ? (int) (System.currentTimeMillis() - lastReqSentTime) / 1000 : 0;
        return String.format(
                "%s: [ %s, %18s%s]",
                getVersion(),
                channel.getPeerIdShort(),
                peerState,
                waitResp > 5 ? ", wait " + waitResp + "s" : " ");
    }
}
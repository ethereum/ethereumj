package org.ethereum.net.par.handler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.ChannelHandlerContext;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.SnapshotManifest;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.BlockStore;
import org.ethereum.db.StateSource;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.par.ParVersion;
import org.ethereum.net.par.message.GetSnapshotManifestMessage;
import org.ethereum.net.par.message.ParMessage;
import org.ethereum.net.par.message.ParStatusMessage;
import org.ethereum.net.par.message.SnapshotManifestMessage;
import org.ethereum.sync.PeerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.ethereum.sync.PeerState.IDLE;

/**
 * Warp synchronization (PAR1 / Parity) Handler
 */
@Component("Par1")
@Scope("prototype")
public class Par1 extends ParHandler {

    protected PeerState peerState = IDLE;

    private final static Logger logger = LoggerFactory.getLogger("net");

    private static final ParVersion version = ParVersion.PAR1;

    private MessageQueue msgQueue = null;

    @Autowired
    private StateSource stateSource;

    private boolean requestedSnapshotManifest = false;
    private SettableFuture<SnapshotManifest> requestSnapshotManifestFuture;
    protected long lastReqSentTime;

    protected Block bestBlock;
    protected EthereumListener listener = new EthereumListenerAdapter() {
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            bestBlock = block;
        }
    };


    @Autowired
    public Par1(final SystemProperties config, final Blockchain blockchain, BlockStore blockStore,
                final CompositeEthereumListener ethereumListener) {
        super(version, config, blockchain, blockStore, ethereumListener);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ParMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        switch (msg.getCommand()) {
            case STATUS:
                processStatus((ParStatusMessage) msg, ctx);
                break;
//            case GET_SNAPSHOT_MANIFEST:
//                processGetSnapshotManifest((GetSnapshotManifestMessage) msg);
//                break;
            case SNAPSHOT_MANIFEST:
                processManifest((SnapshotManifestMessage) msg);
                break;
            default:
                break;
        }
    }


    protected synchronized void processStatus(ParStatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {

        try {

            if (!Arrays.equals(msg.getGenesisHash(), config.getGenesis().getHash())) {
                if (!peerDiscoveryMode) {
                    logger.debug("Removing ParHandler for {} due to protocol incompatibility", ctx.channel().remoteAddress());
                }
                disconnect(ReasonCode.INCOMPATIBLE_PROTOCOL);
                ctx.pipeline().remove(this); // Peer is not compatible for the 'eth' sub-protocol
                return;
            }

            if (msg.getNetworkId() != config.networkId()) {
                disconnect(ReasonCode.NULL_IDENTITY);
                return;
            }

            // TODO: splitting of status proceeding should be done in better way
            // TODO: We should unlock processing for all methods after ETH passed all checkings
            ethHandler.sendGetBlockHeaders(msg.getBestHash(), 1, 0, false);
        } catch (NoSuchElementException e) {
            logger.debug("ParHandler already removed");
        }
    }

    @Override
    public synchronized ListenableFuture<SnapshotManifest> requestManifest() {
        if (peerState != PeerState.IDLE) return null;

        GetSnapshotManifestMessage msg = new GetSnapshotManifestMessage();

        requestSnapshotManifestFuture = SettableFuture.create();
        requestedSnapshotManifest = true;
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();

        peerState = PeerState.MANIFEST_RETRIEVING;
        return requestSnapshotManifestFuture;
    }

    protected synchronized void processManifest(SnapshotManifestMessage msg) {
        if (!requestedSnapshotManifest) {
            logger.debug("Received SnapshotManifestMessage when requestedSnapshotManifest == null. Dropping peer " +
                    channel);
            dropConnection();
        }

        // TODO: Add checkings

        SnapshotManifest snapshotManifest = new SnapshotManifest(
                msg.getStateHashes(),
                msg.getBlockHashes(),
                msg.getStateRoot(),
                msg.getBlockNumber(),
                msg.getBlockHash()
        );

        requestSnapshotManifestFuture.set(snapshotManifest);

        requestedSnapshotManifest = false;
        requestSnapshotManifestFuture = null;
        lastReqSentTime = 0;
        peerState = PeerState.IDLE;
    }
}

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
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.SnapshotManager;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.par.ParVersion;
import org.ethereum.net.par.message.GetSnapshotDataMessage;
import org.ethereum.net.par.message.GetSnapshotManifestMessage;
import org.ethereum.net.par.message.ParMessage;
import org.ethereum.net.par.message.ParStatusMessage;
import org.ethereum.net.par.message.SnapshotDataMessage;
import org.ethereum.net.par.message.SnapshotManifestMessage;
import org.ethereum.sync.PeerState;
import org.ethereum.sync.SyncManager;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLPElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

/**
 * Warp synchronization (PAR1 / Parity) Handler
 */
@Component("Par1")
@Scope("prototype")
public class Par1 extends ParHandler {

    @Autowired
    SnapshotManager snapshotManager;

    @Autowired
    SyncManager syncManager;

    @Autowired
    protected BlockStore blockstore;

    private final static Logger logger = LoggerFactory.getLogger("net");

    public static final ParVersion VERSION = ParVersion.PAR1;

    private boolean requestedSnapshotManifest = false;
    private SettableFuture<SnapshotManifest> requestSnapshotManifestFuture;

    private ByteArrayWrapper requestedSnapshotData = null;
    private SettableFuture<RLPElement> requestSnapshotDataFuture;

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
        super(VERSION, config, blockchain, blockStore, ethereumListener);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ParMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        switch (msg.getCommand()) {
            case STATUS:
                processStatus((ParStatusMessage) msg, ctx);
                break;
            case GET_SNAPSHOT_MANIFEST:
                processGetSnapshotManifest((GetSnapshotManifestMessage) msg);
                break;
            case SNAPSHOT_MANIFEST:
                processManifest((SnapshotManifestMessage) msg);
                break;
            case GET_SNAPSHOT_DATA:
                processGetData((GetSnapshotDataMessage) msg);
                break;
            case SNAPSHOT_DATA:
                processData((SnapshotDataMessage) msg);
                break;
            default:
                break;
        }
    }

    @Override
    public synchronized void sendStatus() {
        byte protocolVersion = getVersion().getCode();
        int networkId = config.networkId();

        final BigInteger totalDifficulty;
        final byte[] bestHash;

        if (syncManager.isFastSyncRunning()) {
            // while fastsync is not complete reporting block #0
            // until all blocks/receipts are downloaded
            bestHash = blockstore.getBlockHashByNumber(0);
            Block genesis = blockstore.getBlockByHash(bestHash);
            totalDifficulty = genesis.getDifficultyBI();
        } else {
            // Getting it from blockstore, not blocked by blockchain sync
            bestHash = blockstore.getBestBlock().getHash();
            totalDifficulty = blockchain.getTotalDifficulty();
        }

        SnapshotManifest manifest = snapshotManager.getManifest();
        byte[] snapshotBlockHash = manifest == null ? null : manifest.getBlockHash();
        long snapshotBlockNumber = manifest == null ? 0 : manifest.getBlockNumber();

        ParStatusMessage msg = new ParStatusMessage(protocolVersion, networkId,
                ByteUtil.bigIntegerToBytes(totalDifficulty), bestHash, config.getGenesis().getHash(),
                snapshotBlockHash, snapshotBlockNumber);
        sendMessage(msg);
    }

    protected synchronized void processStatus(ParStatusMessage msg, ChannelHandlerContext ctx) throws InterruptedException {
        StatusMessage ethMsg = new StatusMessage(
                msg.getProtocolVersion(),
                msg.getNetworkId(),
                msg.getTotalDifficulty(),
                msg.getBestHash(),
                msg.getGenesisHash()
        );
        ethHandler.processStatus(ethMsg, ctx);
        this.snapshotManifest = new SnapshotManifest(msg.getSnapshotNumber(), msg.getSnapshotHash());
        this.statusPassed = true;
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
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing SnapshotManifestMessage for block #{}",
                channel.getPeerIdShort(),
                msg.getBlockNumber()
        );
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

    protected synchronized void processGetSnapshotManifest(GetSnapshotManifestMessage msg) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetSnapshotManifestMessage",
                channel.getPeerIdShort()
        );

        sendMessage(new SnapshotManifestMessage(snapshotManager.getManifest()));
    }

    @Override
    public synchronized ListenableFuture<RLPElement> requestSnapshotData(byte[] snapshotHash) {
        if (peerState != PeerState.IDLE) return null;
        peerState = PeerState.SNAPSHOT_DATA_RETRIEVING;

        ByteArrayWrapper hash = new ByteArrayWrapper(snapshotHash);
        GetSnapshotDataMessage msg = new GetSnapshotDataMessage(hash);

        requestSnapshotDataFuture = SettableFuture.create();
        requestedSnapshotData = hash;
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();

        return requestSnapshotDataFuture;
    }

    protected synchronized void processData(SnapshotDataMessage msg) {
        if (requestedSnapshotData == null) {
            logger.debug("Received SnapshotDataMessage when requestedSnapshotData == null. Dropping peer " +
                    channel);
            dropConnection();
        }
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing SnapshotDataMessage, chunk size: {} bytes",
                channel.getPeerIdShort(),
                msg.getEncoded().length
        );
        requestSnapshotDataFuture.set(msg.getChunkData());

        requestedSnapshotData = null;
        requestSnapshotDataFuture = null;
        lastReqSentTime = 0;
        peerState = PeerState.IDLE;
    }

    protected synchronized void processGetData(GetSnapshotDataMessage msg) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetSnapshotDataMessage, hash: {}",
                channel.getPeerIdShort(),
                Hex.toHexString(msg.getChunkHash())
        );

        byte[] chunkDataBytes = snapshotManager.getChunk(msg.getChunkHash());
        ByteArrayWrapper chunkData = chunkDataBytes == null ?
                new ByteArrayWrapper(new byte[0]) : new ByteArrayWrapper(chunkDataBytes);

        SnapshotDataMessage dataMessage = new SnapshotDataMessage(chunkData);
        sendMessage(dataMessage);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if (requestSnapshotDataFuture != null)
            requestSnapshotDataFuture.setException(new RuntimeException("Disconnected"));
        if (requestSnapshotManifestFuture != null)
            requestSnapshotManifestFuture.setException(new RuntimeException("Disconnected"));
    }

    @Override
    public String getSyncStats() {
        double chunksPerSec = 1d * channel.getNodeStatistics().par1ChunkBytesReceived.get() / channel.getNodeStatistics().par1ChunksRetrieveTime.get();
        double missChunksRatio = 1 - (double) channel.getNodeStatistics().par1ChunksReceived.get() / channel.getNodeStatistics().par1ChunksRequested.get();
        return super.getSyncStats() + String.format("\tKB/sec: %1$.2f, miss: %2$.2f", chunksPerSec, missChunksRatio);
    }
}

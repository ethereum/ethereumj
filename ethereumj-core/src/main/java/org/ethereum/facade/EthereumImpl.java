package org.ethereum.facade;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.core.PendingState;
import org.ethereum.core.Repository;
import org.ethereum.crypto.ECKey;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.listener.GasPriceTracker;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.BlockLoader;
import org.ethereum.manager.WorldManager;
import org.ethereum.mine.BlockMiner;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.shh.Whisper;
import org.ethereum.net.submit.TransactionExecutor;
import org.ethereum.net.submit.TransactionTask;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FutureAdapter;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
@Component
public class EthereumImpl implements Ethereum {

    private static final Logger logger = LoggerFactory.getLogger("facade");
    private static final Logger gLogger = LoggerFactory.getLogger("general");

    @Autowired
    WorldManager worldManager;

    @Autowired
    AdminInfo adminInfo;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    ApplicationContext ctx;

    @Autowired
    BlockLoader blockLoader;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    Whisper whisper;

    @Autowired
    PendingState pendingState;

    @Autowired
    SystemProperties config;

    @Autowired
    CompositeEthereumListener compositeEthereumListener;

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();


    private GasPriceTracker gasPriceTracker = new GasPriceTracker();

    public EthereumImpl() {
        System.out.println();
    }

    @PostConstruct
    public void init() {
        compositeEthereumListener.addListener(gasPriceTracker);

        gLogger.info("EthereumJ node started: enode://" + Hex.toHexString(config.nodeId()) + "@" + config.externalIp() + ":" + config.listenPort());
    }

    @Override
    public void startPeerDiscovery() {
        worldManager.startPeerDiscovery();
    }

    @Override
    public void stopPeerDiscovery() {
        worldManager.stopPeerDiscovery();
    }

    @Override
    public void connect(InetAddress addr, int port, String remoteId) {
        connect(addr.getHostName(), port, remoteId);
    }

    @Override
    public void connect(final String ip, final int port, final String remoteId) {
        logger.info("Connecting to: {}:{}", ip, port);
        final PeerClient peerClient = ctx.getBean(PeerClient.class);
        peerClient.connectAsync(ip, port, remoteId, false);
    }

    @Override
    public void connect(Node node) {
        connect(node.getHost(), node.getPort(), Hex.toHexString(node.getId()));
    }

    @Override
    public org.ethereum.facade.Blockchain getBlockchain() {
        return (org.ethereum.facade.Blockchain) worldManager.getBlockchain();
    }

    public ImportResult addNewMinedBlock(Block block) {
        ImportResult importResult = worldManager.getBlockchain().tryToConnect(block);
        if (importResult == ImportResult.IMPORTED_BEST) {
            channelManager.sendNewBlock(block, null);
        }
        return importResult;
    }

    @Override
    public BlockMiner getBlockMiner() {
        return ctx.getBean(BlockMiner.class);
    }

    @Override
    public void addListener(EthereumListener listener) {
        worldManager.addListener(listener);
    }

    @Override
    public void close() {
        logger.info("Shutting down Ethereum instance...");
        worldManager.close();
        ((AbstractApplicationContext) getApplicationContext()).close();
    }

    @Override
    public PeerClient getDefaultPeer() {

        PeerClient peer = worldManager.getActivePeer();
        if (peer == null) {

            peer = new PeerClient();
            worldManager.setActivePeer(peer);
        }
        return peer;
    }

    @Override
    public boolean isConnected() {
        return worldManager.getActivePeer() != null;
    }

    @Override
    public Transaction createTransaction(BigInteger nonce,
                                         BigInteger gasPrice,
                                         BigInteger gas,
                                         byte[] receiveAddress,
                                         BigInteger value, byte[] data) {

        byte[] nonceBytes = ByteUtil.bigIntegerToBytes(nonce);
        byte[] gasPriceBytes = ByteUtil.bigIntegerToBytes(gasPrice);
        byte[] gasBytes = ByteUtil.bigIntegerToBytes(gas);
        byte[] valueBytes = ByteUtil.bigIntegerToBytes(value);

        return new Transaction(nonceBytes, gasPriceBytes, gasBytes,
                receiveAddress, valueBytes, data);
    }


    @Override
    public Future<Transaction> submitTransaction(Transaction transaction) {

        TransactionTask transactionTask = new TransactionTask(transaction, channelManager);

        final Future<List<Transaction>> listFuture =
                TransactionExecutor.instance.submitTransaction(transactionTask);

        pendingState.addPendingTransaction(transaction);

        return new FutureAdapter<Transaction, List<Transaction>>(listFuture) {
            @Override
            protected Transaction adapt(List<Transaction> adapteeResult) throws ExecutionException {
                return adapteeResult.get(0);
            }
        };
    }

    @Override
    public TransactionReceipt callConstant(Transaction tx, Block block) {
        if (tx.getSignature() == null) {
            tx.sign(ECKey.fromPrivate(new byte[32]));
        }
        return callConstantImpl(tx, block).getReceipt();
    }

    public BlockSummary replayBlock(Block block) {
        List<TransactionReceipt> receipts = new ArrayList<>();
        List<TransactionExecutionSummary> summaries = new ArrayList<>();

        Repository repository = ((Repository) worldManager.getRepository())
                .getSnapshotTo(block.getStateRoot())
                .startTracking();

        try {
            for (Transaction tx : block.getTransactionsList()) {
                org.ethereum.core.TransactionExecutor executor = commonConfig.transactionExecutor(
                        tx, block.getCoinbase(), repository, worldManager.getBlockStore(),
                        programInvokeFactory, block, worldManager.getListener(), 0);

                executor.setLocalCall(true);
                executor.init();
                executor.execute();
                executor.go();

                TransactionExecutionSummary summary = executor.finalization();
                TransactionReceipt receipt = executor.getReceipt();
                // TODO: change to repository.getRoot() after RepositoryTrack implementation
                receipt.setPostTxState(ArrayUtils.EMPTY_BYTE_ARRAY);
                receipts.add(receipt);
                summaries.add(summary);
            }
        } finally {
            repository.rollback();
        }

        return new BlockSummary(block, new HashMap<byte[], BigInteger>(), receipts, summaries);
    }

    private org.ethereum.core.TransactionExecutor callConstantImpl(Transaction tx, Block block) {

        Repository repository = ((Repository) worldManager.getRepository())
                .getSnapshotTo(block.getStateRoot())
                .startTracking();

        try {
            org.ethereum.core.TransactionExecutor executor = commonConfig.transactionExecutor
                    (tx, block.getCoinbase(), repository, worldManager.getBlockStore(),
                            programInvokeFactory, block, new EthereumListenerAdapter(), 0)
                    .setLocalCall(true);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            return executor;
        } finally {
            repository.rollback();
        }
    }

    @Override
    public ProgramResult callConstantFunction(String receiveAddress,
                                              CallTransaction.Function function, Object... funcArgs) {
        return callConstantFunction(receiveAddress, ECKey.fromPrivate(new byte[32]), function, funcArgs);
    }

    @Override
    public ProgramResult callConstantFunction(String receiveAddress, ECKey senderPrivateKey,
                                              CallTransaction.Function function, Object... funcArgs) {
        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                receiveAddress, 0, function, funcArgs);
        tx.sign(senderPrivateKey);
        Block bestBlock = worldManager.getBlockchain().getBestBlock();

        return callConstantImpl(tx, bestBlock).getResult();
    }

    @Override
    public org.ethereum.facade.Repository getRepository() {
        return worldManager.getRepository();
    }

    @Override
    public org.ethereum.facade.Repository getPendingState() {
        return (org.ethereum.facade.Repository) worldManager.getPendingState().getRepository();
    }

    @Override
    public org.ethereum.facade.Repository getSnapshotTo(byte[] root) {

        Repository repository = (Repository) worldManager.getRepository();
        org.ethereum.facade.Repository snapshot = (org.ethereum.facade.Repository) repository.getSnapshotTo(root);

        return snapshot;
    }

    @Override
    public AdminInfo getAdminInfo() {
        return adminInfo;
    }

    @Override
    public ChannelManager getChannelManager() {
        return channelManager;
    }


    @Override
    public List<Transaction> getWireTransactions() {
        return worldManager.getPendingState().getPendingTransactions();
    }

    @Override
    public List<Transaction> getPendingStateTransactions() {
        return worldManager.getPendingState().getPendingTransactions();
    }

    @Override
    public BlockLoader getBlockLoader() {
        return blockLoader;
    }

    @Override
    public Whisper getWhisper() {
        return whisper;
    }

    @Override
    public long getGasPrice() {
        return gasPriceTracker.getGasPrice();
    }

    @Override
    public void exitOn(long number) {
        worldManager.getBlockchain().setExitOn(number);
    }

    /**
     * For testing purposes and 'hackers'
     */
    public ApplicationContext getApplicationContext() {
        return ctx;
    }
}

package org.ethereum.facade;

import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.BlockLoader;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.vm.program.ProgramResult;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Roman Mandeleil
 * @since 27.07.2014
 */
public interface Ethereum {

    /**
     * Find a peer but not this one
     *
     * @param excludePeer - peer to exclude
     * @return online peer if available otherwise null
     */
    PeerInfo findOnlinePeer(PeerInfo excludePeer);

    /**
     * Find an online peer but not from excluded list
     *
     * @param excludePeerSet - peers to exclude
     * @return online peer if available otherwise null
     */
    PeerInfo findOnlinePeer(Set<PeerInfo> excludePeerSet);

    /**
     * @return online peer if available
     */
    PeerInfo findOnlinePeer();


    /**
     * That block will block until online peer was found.
     *
     * @return online peer.
     */
    PeerInfo waitForOnlinePeer();

    /*
     *
     *  The set of peers returned
     *  by the method is not thread
     *  safe then should be traversed
     *  sync safe:
     *    synchronized (peers){
     *        for (final Peer peer : newPeers) {}
     *    }
     *
     */
    Set<PeerInfo> getPeers();

    void startPeerDiscovery();

    void stopPeerDiscovery();

    void connect(InetAddress addr, int port, String remoteId);

    void connect(String ip, int port, String remoteId);

    void connect(Node node);

    Blockchain getBlockchain();

    void addListener(EthereumListener listener);

    PeerClient getDefaultPeer();

    boolean isConnected();

    void close();

    /**
     * Factory for general transaction
     *
     *
     * @param nonce - account nonce, based on number of transaction submited by
     *                this account
     * @param gasPrice - gas price bid by miner , the user ask can be based on
     *                   lastr submited block
     * @param gas - the quantity of gas requested for the transaction
     * @param receiveAddress - the target address of the transaction
     * @param value - the ether value of the transaction
     * @param data - can be init procedure for creational transaction,
     *               also msg data for invoke transaction for only value
     *               transactions this one is empty.
     * @return newly created transaction
     */
    Transaction createTransaction(BigInteger nonce,
                                 BigInteger gasPrice,
                                 BigInteger gas,
                                 byte[] receiveAddress,
                                 BigInteger value, byte[] data);


    /**
     * @param transaction submit transaction to the net, return option to wait for net
     *                    return this transaction as approved
     */
    Future<Transaction> submitTransaction(Transaction transaction);


    /**
     * Call a contract function locally without sending transaction to the network
     * and without changing contract storage.
     * @param receiveAddress hex encoded contract address
     * @param function  contract function
     * @param funcArgs  function arguments
     * @return function result. The return value can be fetched via {@link ProgramResult#getHReturn()}
     * and decoded with {@link org.ethereum.core.CallTransaction.Function#decodeResult(byte[])}.
     */
    ProgramResult callConstantFunction(String receiveAddress, CallTransaction.Function function,
                                       Object... funcArgs);

    /**
     * @return wallet object which is the manager
     *         of internal accounts
     */
    Wallet getWallet();


    /**
     * @return - repository for all state data.
     */
    Repository getRepository();


    public void init();
//  2.   // is blockchain still loading - if buffer is not empty

    Repository getSnapshootTo(byte[] root);

    AdminInfo getAdminInfo();

    ChannelManager getChannelManager();

    Set<Transaction> getPendingTransactions();

    BlockLoader getBlockLoader();

    void exitOn(long number);
}

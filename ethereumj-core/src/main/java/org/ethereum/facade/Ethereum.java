package org.ethereum.facade;

import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.server.ChannelManager;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 27/07/2014 09:11
 */

public interface Ethereum {

    /**
     * Find a peer but not this one
     * @param excludePeer - peer to exclude
     * @return online peer if available otherwise null
     */
    public PeerInfo findOnlinePeer(PeerInfo excludePeer) ;

    /**
     * Find an online peer but not from excluded list
     *
     * @param excludePeerSet - peers to exclude
     * @return online peer if available otherwise null
     */
    public PeerInfo findOnlinePeer(Set<PeerInfo> excludePeerSet) ;

    /**
     * @return online peer if available
     */
    public PeerInfo findOnlinePeer();


    /**
     * That block will block until online peer was found.
     *
     * @return online peer.
     */
    public PeerInfo waitForOnlinePeer();

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
    public Set<PeerInfo> getPeers();

    public void startPeerDiscovery();
    public void stopPeerDiscovery();

    public void connect(InetAddress addr, int port);
    public void connect(String ip, int port);

    public Blockchain getBlockchain();

    public boolean isBlockchainLoading();

    public void addListener(EthereumListener listener);

    public PeerClient getDefaultPeer();

    public boolean isConnected();

    public void close();

    /**
     * Factory for general transaction
     *
     *
     * @param nonce - account nonce, based on number of transaction submited by
     *                this account
     * @param gasPrice - gas price bid by miner , the user ask can be based on
     *                   lastr submited block
     * @param gas - the quantity of gas requested for the transaction
     * @param recieveAddress - the target address of the transaction
     * @param value - the ether value of the transaction
     * @param data - can be init procedure for creational transaction,
     *               also msg data for invoke transaction for only value
     *               transactions this one is empty.
     * @return newly created transaction
     */
    public Transaction createTransaction(BigInteger nonce,
                                         BigInteger gasPrice,
                                         BigInteger gas,
                                         byte[] recieveAddress,
                                         BigInteger value, byte[] data);


                                         /**
                                          *
                                          * @param transaction - submit transaction to the net, return
                                          *                      option to wait for net return this transaction
                                          *                      as approved
                                          * @return
                                          */
    public Future<Transaction> submitTransaction(Transaction transaction);


    /**
     * @return wallet object which is the manager
     *         of internal accounts
     */
    public Wallet getWallet();


    /**
     * @return - repository for all state data.
     */
    public Repository getRepository();


    public void init();
//  2.   // is blockchain still loading - if buffer is not empty

    public AdminInfo getAdminInfo();

    public ChannelManager getChannelManager();

}

package org.ethereum.facade;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.submit.TransactionExecutor;
import org.ethereum.net.submit.TransactionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 27/07/2014 09:12
 */

public class EthereumImpl implements Ethereum {

    private static final Logger logger = LoggerFactory.getLogger("facade");

    public EthereumImpl() {
        WorldManager.getInstance().loadBlockchain();
    }

    /**
     * Find a peer but not this one
     * @param peerData - peer to exclude
     * @return online peer
     */
    @Override
    public PeerData findOnlinePeer(PeerData peerData) {

        Set<PeerData> excludePeers = new HashSet<>();
        excludePeers.add(peerData);

        return findOnlinePeer(excludePeers);
    }

    @Override
    public PeerData findOnlinePeer() {

        Set<PeerData> excludePeers = new HashSet<>();
        return findOnlinePeer(excludePeers);
    }


    @Override
    public PeerData findOnlinePeer(Set<PeerData> excludePeers)  {
        logger.info("Looking for online peers...");

        final EthereumListener listener = WorldManager.getInstance().getListener();
        if (listener != null) {
            listener.trace("Looking for online peer");
        }

        WorldManager.getInstance().startPeerDiscovery();

        final Set<PeerData> peers = WorldManager.getInstance().getPeers();
        synchronized (peers) {

            for (PeerData peer : peers) { // it blocks until a peer is available.

                if (peer.isOnline() &&   !excludePeers.contains(peer)) {

                    logger.info("Found peer: {}", peer.toString());

                    if (listener != null)
                        listener.trace(String.format("Found online peer: [ %s ]", peer.toString()));

                    return peer;
                }
            }
        }
        return null;
    }


    @Override
    public PeerData waitForOnlinePeer(){

        PeerData peer = null;
        while(peer == null){

            try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
            peer = this.findOnlinePeer();
        }

        return peer;
    }


    @Override
    public Set<PeerData> getPeers() {
        return WorldManager.getInstance().getPeers();
    }

    @Override
    public void startPeerDiscovery(){
        WorldManager.getInstance().startPeerDiscovery();
    }


    @Override
    public void stopPeerDiscovery(){
        WorldManager.getInstance().stopPeerDiscovery();
    }

    @Override
    public void connect(InetAddress addr, int port){
        connect(addr.getHostName(), port);
    }

    @Override
    public void connect(String ip, int port){

        logger.info("Connecting to: {}:{}", ip, port);
        new ClientPeer().connect(ip,
                port);
    }

    @Override
    public Blockchain getBlockChain() {
        return WorldManager.getInstance().getBlockchain();
    }

    @Override
    public void addListener(EthereumListener listener) {
        WorldManager.getInstance().addListener(listener);
    }

    @Override
    public boolean isBlockChainLoading() {
        return WorldManager.getInstance().isBlockChainLoading();
    }

    @Override
    public void close() {
        WorldManager.getInstance().close();
    }

    @Override
    public ClientPeer getDefaultPeer(){

        ClientPeer peer = WorldManager.getInstance().getActivePeer();
        if (peer == null){

            peer = new ClientPeer();
            WorldManager.getInstance().setActivePeer(peer);
        }

        return peer;
    }

    @Override
    public boolean isConnected() {
        return WorldManager.getInstance().getActivePeer() != null;
    }


    @Override
    public Transaction createTransaction(byte[] nonce, byte[] gasPrice, byte[] gas,
                                      byte[] recieveAddress, byte[] value, byte[] data ){

        Transaction tx = new Transaction(nonce, gasPrice, gas,
                recieveAddress, value, data);

        return tx;
    }


    @Override
    public Future<Transaction> submitTransaction(Transaction transaction){

        TransactionTask transactionTask = new TransactionTask(transaction);
        Future<Transaction> future = TransactionExecutor.instance.submitTransaction(transactionTask);

        return future;
    }


    @Override
    public Wallet getWallet(){
        return WorldManager.getInstance().getWallet();
    }


    @Override
    public RepositoryImpl getRepository(){
        return WorldManager.getInstance().getRepository();
    }

}

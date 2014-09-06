package org.ethereum.facade;

import org.ethereum.core.Block;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;

import java.net.InetAddress;
import java.util.Set;

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
    public PeerData findOnlinePeer(PeerData excludePeer) ;


    /**
     * Find an online peer but not from excluded list
     *
     * @param excludePeerSet - peers to exclude
     * @return online peer if available otherwise null
     */
    public PeerData findOnlinePeer(Set<PeerData> excludePeerSet) ;

    /**
     * @return online peer if available
     */
    public PeerData findOnlinePeer();


    /**
     * That block will block until online peer was found.
     *
     * @return online peer.
     */
    public PeerData waitForOnlinePeer();

    /*
     *
     *  The set of peers returned
     *  by the method is not thread
     *  safe then should be traversed
     *  sync safe:
     *    synchronized (peers){
     *        for (final PeerData peer : newPeers) {}
     *    }
     *
     */
    public Set<PeerData> getPeers();

    public void startPeerDiscovery();
    public void stopPeerDiscovery();

    public void connect(InetAddress addr, int port);
    public void connect(String ip, int port);

    public Blockchain getBlockChain();

    public void addListener(EthereumListener listener);

    public void loadBlockChain();


    public ClientPeer getDefaultPeer();

    public void close();


//  1.   WorldManager.getInstance().getWallet();
//  2.   // is blockchain still loading - if buffer is not empty



}

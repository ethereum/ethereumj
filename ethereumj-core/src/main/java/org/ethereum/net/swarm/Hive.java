package org.ethereum.net.swarm;

import org.ethereum.net.swarm.bzz.BzzPeersMessage;
import org.ethereum.net.swarm.bzz.BzzProtocol;
import org.ethereum.net.swarm.kademlia.stub.Address;
import org.ethereum.net.swarm.kademlia.stub.NodeRecord;

import java.util.Collection;

/**
 * Created by Admin on 18.06.2015.
 */
public class Hive {

    NetStore netStore;

    public void start(Address address) {}
    public void stop() {}

    public void addPeer(BzzProtocol peer) {}
    public void removePeer(BzzProtocol peer) {}
    public Collection<BzzProtocol> getPeers(Key key, int max) { return null;}
    public NodeRecord newNodeRecord(BzzProtocol.PeerAddr addr) {return null;}
    public void addPeerRecords(BzzPeersMessage req) {}
}

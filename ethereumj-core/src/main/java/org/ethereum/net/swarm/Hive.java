package org.ethereum.net.swarm;

import org.ethereum.net.swarm.bzz.BzzPeersMessage;
import org.ethereum.net.swarm.bzz.BzzProtocol;
import org.ethereum.net.swarm.bzz.PeerAddress;
import org.ethereum.net.swarm.kademlia.stub.Address;
import org.ethereum.net.swarm.kademlia.stub.NodeRecord;
import org.hibernate.internal.util.collections.IdentitySet;

import java.util.*;

/**
 * Created by Admin on 18.06.2015.
 */
public class Hive {

    NetStore netStore;

    public void start(Address address) {}
    public void stop() {}

    public void addPeer(BzzProtocol peer) {}
    public void removePeer(BzzProtocol peer) {}
    public Collection<PeerAddress> getNodes(Key key, int max) { return null;}
    public Collection<BzzProtocol> getPeers(Key key, int max) { return null;}
    public NodeRecord newNodeRecord(BzzProtocol.PeerAddr addr) {return null;}
    public void addPeerRecords(BzzPeersMessage req) {
        for (PeerAddress peerAddress : req.getPeers()) {

        }
    }

    protected void peersAdded() {
        for (HiveTask task : new ArrayList<HiveTask>(hiveTasks)) {
            if (!task.peersAdded()) {
                hiveTasks.remove(task);
            }
        }
    }

    public void addTask(HiveTask t) {
        if (t.peersAdded()) {
            hiveTasks.add(t);
        }
    }

    private Set<HiveTask> hiveTasks = new IdentitySet();

    public abstract class HiveTask {
        Key targetKey;
        Set<BzzProtocol> processedPeers = new IdentitySet();
        long expireTime;
        int maxPeers;

        public HiveTask(Key targetKey, long timeout, int maxPeers) {
            this.targetKey = targetKey;
            this.expireTime = Util.curTime() + timeout;
            this.maxPeers = maxPeers;
        }

        public boolean peersAdded() {
            if (Util.curTime() > expireTime) return false;
            Collection<BzzProtocol> peers = getPeers(targetKey, maxPeers);
            for (BzzProtocol peer : peers) {
                if (!processedPeers.contains(peer)) {
                    processPeer(peer);
                    processedPeers.add(peer);
                    if (processedPeers.size() > maxPeers) {
                        return false;
                    }
                }
            }
            return true;
        }

        protected abstract void processPeer(BzzProtocol peer);
    }
}

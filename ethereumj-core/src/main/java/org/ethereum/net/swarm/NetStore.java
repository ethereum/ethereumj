package org.ethereum.net.swarm;

import org.ethereum.net.p2p.Peer;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.swarm.bzz.*;
import org.ethereum.net.swarm.kademlia.stub.Address;

import java.util.*;

/**
 * Created by Admin on 18.06.2015.
 */
public class NetStore implements ChunkStore {

    final int requesterCount = 3;

    enum EntryReqStatus {
        Searching,
        Found
    }

    public class ChunkRequest {
        EntryReqStatus status = EntryReqStatus.Searching;
        Map<Long, Collection<BzzRetrieveReqMessage>> requesters = new HashMap<>();
    }

    public LocalStore localStore;
    public Hive hive;
    public PeerInfo self;
    String path;

    public NetStore(LocalStore localStore, Hive hive) {
        this.localStore = localStore;
        this.hive = hive;
    }

    public void start(PeerInfo self, Object connectListener /* ? */) {
        this.self = self;
        hive.start(new Address(self));
    }

    public void stop() {
        hive.stop();
    }

    /******************************************
     *    Put methods
     ******************************************/

    // called from dpa, entrypoint for *local* chunk store requests
    @Override
    public void put(Chunk chunk) {
//        Chunk localChunk = localStore.get(chunk.getKey()); // ???
        putImpl(chunk);
    }

    // store logic common to local and network chunk store requests
    void putImpl(Chunk chunk) {
        localStore.put(chunk);
        if (chunkRequestMap.get(chunk.getKey()) != null &&
                chunkRequestMap.get(chunk.getKey()).status == EntryReqStatus.Searching) {
            // If this is response to retrieve message
            chunkRequestMap.get(chunk.getKey()).status = EntryReqStatus.Found;

            // Resend to all (max [3]) requesters
            propagateResponse(chunk);
            // TODO remove chunkRequest from map (memleak) ???
        } else {
            // If local, broadcast store request to hive peers
            store(chunk);
        }
    }

    // the entrypoint for network store requests
    public void addStoreRequest(BzzStoreReqMessage msg) {
        Chunk chunk = localStore.get(msg.getKey()); // TODO hasKey() should be faster
        if (chunk == null) {
            // If not in local store yet
            // but remember the request source to exclude it from store broadcast
            chunk = new Chunk(msg.getKey(), msg.getData());
            chunkSourceAddr.put(chunk, msg.getPeer().peer);
            putImpl(chunk);
        }
    }

    // once a chunk is found propagate it its requesters unless timed out
    private void propagateResponse(Chunk chunk) {
        for (Map.Entry<Long, Collection<BzzRetrieveReqMessage>> e :
                chunkRequestMap.get(chunk.getKey()).requesters.entrySet()) {
            BzzStoreReqMessage msg = new BzzStoreReqMessage(e.getKey(), chunk.getKey(), chunk.getData());

            int counter = requesterCount;
            for (BzzRetrieveReqMessage r : e.getValue()) {
                r.getPeer().sendMessage(msg);
                if (--counter < 0) {
                    break;
                }
            }
        }
    }


    // store propagates store requests to specific peers given by the kademlia hive
    // except for peers that the store request came from (if any)
    private void store(Chunk chunk) {
        Peer chunkStoreRequestSource = chunkSourceAddr.get(chunk);
        for (BzzProtocol peer : hive.getPeers(chunk.getKey(), 5)) {
            if (chunkStoreRequestSource != peer.peer) {
                peer.storeRequest(chunk.getKey());
            }
        }
    }

    private Map<Chunk, Peer> chunkSourceAddr = new IdentityHashMap<>();
    private Map<Key, ChunkRequest> chunkRequestMap = new HashMap<>();
//    ChunkRequest getChunkRequest(Key chunkKey) {
//        return chunkRequestMap.get(chunkKey);
//    }
//    void putChunkRequest(Key chunkKey, ChunkRequest req) {
//        chunkRequestMap.put(chunkKey, req);
//    }

    /******************************************
     *    Get methods
     ******************************************/

    @Override
    // Get is the entrypoint for local retrieve requests
    // waits for response or times out
    public Chunk get(Key key) {
        Chunk chunk = getImpl(key);
        if (chunk == null) {
            long id = BzzProtocol.idGenerator.incrementAndGet();
            long timeout = 0; // TODO
            startSearch(id, key, timeout);
            return getImpl(key);
        } else {
            return chunk;
        }
    }

    // retrieve logic common for local and network chunk retrieval
    Chunk getImpl(Key key) {
        Chunk chunk = localStore.get(key);
//        if (chunk == null) {
//            // no data and no request status
////            chunk = new Chunk(key, null); // TODO chunk to be filled
////            localStore.memStore.put(chunk);
//        }
        if (chunkRequestMap.get(key) == null) {
            chunkRequestMap.put(key, new ChunkRequest());
        }

        return chunk;
    }

    // entrypoint for network retrieve requests
    public void addRetrieveRequest(BzzRetrieveReqMessage req) {
        Chunk chunk = getImpl(req.getKey());
        if (chunk == null) {
//            req.timeout = +10sec // TODO ???
        } else {

            ChunkRequest chunkRequest = chunkRequestMap.get(chunk.getKey());
            if (chunkRequest != null) {
                chunkRequest.status = EntryReqStatus.Found;
            }
        }

        long timeout = strategyUpdateRequest(chunkRequestMap.get(req.getKey()), req);

        if (timeout == -1) {
            deliver(req, chunk);
        } else {
            // we might need chunk.req to cache relevant peers response, or would it expire?
            peers(req, chunk, timeout);
            startSearch(req.getId(), req.getKey(), timeout);
        }
    }

    // logic propagating retrieve requests to peers given by the kademlia hive
    // it's assumed that caller holds the lock
    private Chunk startSearch(long id, Key key, long timeout) {
        ChunkRequest chunkRequest = chunkRequestMap.get(key);
        chunkRequest.status = EntryReqStatus.Searching;
//        chunk.req.status = Searching
        Collection<BzzProtocol> peers = hive.getPeers(key, 5);
//        dpaLogger.Debugf("netStore.startSearch: %064x - received %d peers from K???MLI?...", chunk.Key, len(peers))
        BzzRetrieveReqMessage req = new BzzRetrieveReqMessage(id, key/*, timeout*/);
        for (BzzProtocol peer : peers) {
//            dpaLogger.Debugf("netStore.startSearch: sending retrieveRequests to peer [%064x]", req.Key)
//            dpaLogger.Debugf("req.requesters: %v", chunk.req.requesters)

            boolean requester = false;
            out:
            for (Collection<BzzRetrieveReqMessage> chReqColl : chunkRequest.requesters.values()) {
                for (BzzRetrieveReqMessage chReq : chReqColl) {
                    if (peer.addr().equals(chReq.getPeer().addr())) {
                        requester = true;
                        break out;
                    }
                }
            }
            if (!requester) {
                peer.sendMessage(req);
            }
        }
        return null;
    }

    // add peer request the chunk and decides the timeout for the response if still searching
    private long strategyUpdateRequest(ChunkRequest chunkRequest, BzzRetrieveReqMessage req) {
        if (chunkRequest != null && chunkRequest.status == EntryReqStatus.Searching) {
            addRequester(chunkRequest, req);
            return searchingTimeout(chunkRequest, req);
        } else {
            return -1;
        }

    }

    /*
    adds a new peer to an existing open request
    only add if less than requesterCount peers forwarded the same request id so far
    note this is done irrespective of status (searching or found)
    */
    void addRequester(ChunkRequest rs, BzzRetrieveReqMessage req) {
        Collection<BzzRetrieveReqMessage> list = rs.requesters.get(req.getId());
        if (list == null) {
            list = new ArrayList<>();
            rs.requesters.put(req.getId(), list);
        }
        list.add(req);
    }

    // called on each request when a chunk is found,
    // delivery is done by sending a request to the requesting peer
    private void deliver(BzzRetrieveReqMessage req, Chunk chunk) {
        BzzStoreReqMessage msg = new BzzStoreReqMessage(req.getId(), req.getKey(), chunk.getData());
        req.getPeer().sendMessage(msg);
    }

    // the immediate response to a retrieve request,
    // sends relevant peer data given by the kademlia hive to the requester
    private void peers(BzzRetrieveReqMessage req, Chunk chunk, long timeout) {
        Collection<BzzProtocol> peers = hive.getPeers(req.getKey(), (int) req.getMaxPeers());
        List<PeerAddress> peerAddrs = new ArrayList<>();
        for (BzzProtocol peer : peers) {
            peerAddrs.add(peer.getNode());
        }
        BzzPeersMessage msg = new BzzPeersMessage(peerAddrs, req.getKey(), req.getId());
        req.getPeer().sendMessage(msg);
    }

    private long searchingTimeout(ChunkRequest chunkRequest, BzzRetrieveReqMessage req) {
        // TODO
        return 0;
    }
}


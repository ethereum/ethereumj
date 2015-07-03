package org.ethereum.net.swarm;

import org.ethereum.net.p2p.Peer;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.swarm.bzz.*;
import org.ethereum.net.swarm.kademlia.stub.Address;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Admin on 18.06.2015.
 */
public class NetStore implements ChunkStore {

    final int requesterCount = 3;
    final int maxStorePeers = 3;
    final int maxSearchPeers = 6;
    final int timeout = 600 * 1000;

    public final Statter statInMsg = Statter.create("net.swarm.bzz.inMessages");
    public final Statter statOutMsg = Statter.create("net.swarm.bzz.outMessages");
    public final Statter statHandshakes = Statter.create("net.swarm.bzz.handshakes");

    public final Statter statInStoreReq = Statter.create("net.swarm.in.storeReq");
    public final Statter statInGetReq = Statter.create("net.swarm.in.getReq");
    public final Statter statOutStoreReq = Statter.create("net.swarm.out.storeReq");
    public final Statter statOutGetReq = Statter.create("net.swarm.out.getReq");


    enum EntryReqStatus {
        Searching,
        Found
    }

    public class ChunkRequest {
        EntryReqStatus status = EntryReqStatus.Searching;
        Map<Long, Collection<BzzRetrieveReqMessage>> requesters = new HashMap<>();
        List<CompletableFuture<Chunk>> localRequesters = new ArrayList<>();
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
    public synchronized void put(Chunk chunk) {
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
        statInStoreReq.add(1);

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
    private synchronized void propagateResponse(Chunk chunk) {
        ChunkRequest chunkRequest = chunkRequestMap.get(chunk.getKey());
        for (CompletableFuture<Chunk> localRequester : chunkRequest.localRequesters) {
            localRequester.complete(chunk);
        }

        for (Map.Entry<Long, Collection<BzzRetrieveReqMessage>> e :
                chunkRequest.requesters.entrySet()) {
            BzzStoreReqMessage msg = new BzzStoreReqMessage(e.getKey(), chunk.getKey(), chunk.getData());

            int counter = requesterCount;
            for (BzzRetrieveReqMessage r : e.getValue()) {
                r.getPeer().sendMessage(msg);
                statOutStoreReq.add(1);
                if (--counter < 0) {
                    break;
                }
            }
        }
    }


    // store propagates store requests to specific peers given by the kademlia hive
    // except for peers that the store request came from (if any)
    private void store(final Chunk chunk) {
        final Peer chunkStoreRequestSource = chunkSourceAddr.get(chunk);

        hive.addTask(hive.new HiveTask(chunk.getKey(), timeout, maxStorePeers) {
            @Override
            protected void processPeer(BzzProtocol peer) {
                if (chunkStoreRequestSource != peer.peer) {
//                    long id = peer.idGenerator.incrementAndGet();
//                Chunk chunk = netStore.localStore.dbStore.get(key);// ??? Why DB ?
                    BzzStoreReqMessage msg = new BzzStoreReqMessage(chunk.getKey(), chunk.getData());
                    peer.sendMessage(msg);
                }
            }
        });

/*
        for (BzzProtocol peer : hive.getPeers(chunk.getKey(), maxStorePeers)) {
            if (chunkStoreRequestSource != peer.peer) {
                long id = peer.idGenerator.incrementAndGet();
//                Chunk chunk = netStore.localStore.dbStore.get(key);// ??? Why DB ?
                BzzStoreReqMessage msg = new BzzStoreReqMessage(id, chunk.getKey(), chunk.getData());
                peer.sendMessage(msg);
            }
        }
*/
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
        try {
            return getAsync(key).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Future<Chunk> getAsync(Key key) {
        Chunk chunk = localStore.get(key);
        CompletableFuture<Chunk> ret = new CompletableFuture<>();
        if (chunk == null) {
//            long id = BzzProtocol.idGenerator.incrementAndGet();
//            long timeout = 0; // TODO
            ChunkRequest chunkRequest = new ChunkRequest();
            chunkRequest.localRequesters.add(ret);
            chunkRequestMap.put(key, chunkRequest);
            startSearch(-1, key, timeout);
        } else {
            ret.complete(chunk);
        }
        return ret;
    }

    // retrieve logic common for local and network chunk retrieval
    private Chunk getImpl(Key key) {
        Chunk chunk = localStore.get(key);
//        if (chunk == null) {
//            // no data and no request status
////            chunk = new Chunk(key, null); // TODO chunk to be filled
////            localStore.memStore.put(chunk);
//        }
        if (chunkRequestMap.get(key) == null) {
//            chunkRequestMap.put(key, new ChunkRequest());
        }

        return chunk;
    }

    // entrypoint for network retrieve requests
    public void addRetrieveRequest(BzzRetrieveReqMessage req) {
        statInGetReq.add(1);

        Chunk chunk = localStore.get(req.getKey());

        ChunkRequest chunkRequest = chunkRequestMap.get(req.getKey());

        if (chunk == null) {
            peers(req, chunk, 0);
            if (chunkRequest == null) {
                chunkRequestMap.put(req.getKey(), new ChunkRequest());
                long timeout = strategyUpdateRequest(chunkRequestMap.get(req.getKey()), req);
                startSearch(req.getId(), req.getKey(), timeout);
            }
//            req.timeout = +10sec // TODO ???
        } else {

            long timeout = strategyUpdateRequest(chunkRequestMap.get(req.getKey()), req);
            if (chunkRequest != null) {
                chunkRequest.status = EntryReqStatus.Found;
            }
            deliver(req, chunk);
        }

//
//        if (timeout == -1) {
//            deliver(req, chunk);
//        } else {
//            // we might need chunk.req to cache relevant peers response, or would it expire?
//            peers(req, chunk, timeout);
//            if (chunkRequest == null) {
//                chunkRequestMap.put(req.getKey(), new ChunkRequest());
//                startSearch(req.getId(), req.getKey(), timeout);
//            }
//        }
    }

    // logic propagating retrieve requests to peers given by the kademlia hive
    // it's assumed that caller holds the lock
    private Chunk startSearch(long id, Key key, long timeout) {
        final ChunkRequest chunkRequest = chunkRequestMap.get(key);
        chunkRequest.status = EntryReqStatus.Searching;

        final BzzRetrieveReqMessage req = new BzzRetrieveReqMessage(key/*, timeout*/);
        hive.addTask(hive.new HiveTask(key, timeout, maxSearchPeers) {
            @Override
            protected void processPeer(BzzProtocol peer) {
                boolean requester = false;
                out:
                for (Collection<BzzRetrieveReqMessage> chReqColl : chunkRequest.requesters.values()) {
                    for (BzzRetrieveReqMessage chReq : chReqColl) {
                        if (chReq.getPeer().addr().equals(peer.addr())) {
                            requester = true;
                            break out;
                        }
                    }
                }
                if (!requester) {
                    statOutGetReq.add(1);
                    peer.sendMessage(req);
                }
            }
        });

/*
        Collection<BzzProtocol> peers = hive.getPeers(key, maxSearchPeers);
        BzzRetrieveReqMessage req = new BzzRetrieveReqMessage(id, key*/
/*, timeout*//*
);
        for (BzzProtocol peer : peers) {

            boolean requester = false;
            out:
            for (Collection<BzzRetrieveReqMessage> chReqColl : chunkRequest.requesters.values()) {
                for (BzzRetrieveReqMessage chReq : chReqColl) {
                    if (chReq.getPeer().addr().equals(peer.addr())) {
                        requester = true;
                        break out;
                    }
                }
            }
            if (!requester) {
                statOutGetReq.add(1);
                peer.sendMessage(req);
            }
        }
*/
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
        statOutStoreReq.add(1);
    }

    // the immediate response to a retrieve request,
    // sends relevant peer data given by the kademlia hive to the requester
    private void peers(BzzRetrieveReqMessage req, Chunk chunk, long timeout) {
//        Collection<BzzProtocol> peers = hive.getPeers(req.getKey(), maxSearchPeers/*(int) req.getMaxPeers()*/);
//        List<PeerAddress> peerAddrs = new ArrayList<>();
//        for (BzzProtocol peer : peers) {
//            peerAddrs.add(peer.getNode());
//        }
        Collection<PeerAddress> nodes = hive.getNodes(req.getKey(), maxSearchPeers);
        BzzPeersMessage msg = new BzzPeersMessage(new ArrayList<>(nodes), req.getKey(), req.getId());
        req.getPeer().sendMessage(msg);
    }

    private long searchingTimeout(ChunkRequest chunkRequest, BzzRetrieveReqMessage req) {
        // TODO
        return 0;
    }
}


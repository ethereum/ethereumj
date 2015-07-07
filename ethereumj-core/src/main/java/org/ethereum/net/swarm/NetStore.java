package org.ethereum.net.swarm;

import org.ethereum.crypto.HashUtil;
import org.ethereum.net.swarm.bzz.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The main logic of communicating with BZZ peers.
 * The class process local/remote retrieve/store requests and forwards them if necessary
 * to the peers.
 *
 * Magic is happening here!
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class NetStore implements ChunkStore {
    private static NetStore INST;

    public synchronized static NetStore getInstance() {
        if (INST == null) {
            LocalStore localStore = new LocalStore(new MemStore(), new MemStore());
            PeerAddress peerAddress = new PeerAddress(new byte[] {127,0,0,1}, 9999, HashUtil.sha3(new byte[] {0}));
            Hive hive = new Hive(peerAddress);
            INST = new NetStore(localStore, hive);
            INST.start(peerAddress, null);
        }
        return INST;
    }

    private final int requesterCount = 3;
    private final int maxStorePeers = 3;
    private final int maxSearchPeers = 6;
    private final int timeout = 600 * 1000;

    private LocalStore localStore;
    private Hive hive;
    private PeerAddress selfAddress;

    public NetStore(LocalStore localStore, Hive hive) {
        this.localStore = localStore;
        this.hive = hive;
    }

    public void start(PeerAddress self, Object connectListener /* ? */) {
        this.selfAddress = self;
        hive.start();
    }

    public void stop() {
        hive.stop();
    }

    public Hive getHive() {
        return hive;
    }

    public PeerAddress getSelfAddress() {
        return selfAddress;
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
            chunkSourceAddr.put(chunk, msg.getPeer().getNode());
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
        final PeerAddress chunkStoreRequestSource = chunkSourceAddr.get(chunk);

        hive.addTask(hive.new HiveTask(chunk.getKey(), timeout, maxStorePeers) {
            @Override
            protected void processPeer(BzzProtocol peer) {
                if (chunkStoreRequestSource == null || !chunkStoreRequestSource.equals(peer.getNode())) {
                    BzzStoreReqMessage msg = new BzzStoreReqMessage(chunk.getKey(), chunk.getData());
                    peer.sendMessage(msg);
                }
            }
        });
    }

    private Map<Chunk, PeerAddress> chunkSourceAddr = new IdentityHashMap<>();
    private Map<Key, ChunkRequest> chunkRequestMap = new HashMap<>();

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

    // entrypoint for network retrieve requests
    public void addRetrieveRequest(BzzRetrieveReqMessage req) {
        statInGetReq.add(1);

        Chunk chunk = localStore.get(req.getKey());

        ChunkRequest chunkRequest = chunkRequestMap.get(req.getKey());

        if (chunk == null) {
            peers(req, chunk, 0);
            if (chunkRequest == null && !req.getKey().isZero()) {
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
                        if (chReq.getPeer().getNode().equals(peer.getNode())) {
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
        Key key = req.getKey();
        if (key.isZero()) {
            key = req.getPeer().getNode().getAddrKey();
        }
        Collection<PeerAddress> nodes = hive.getNodes(key, maxSearchPeers);
        BzzPeersMessage msg = new BzzPeersMessage(new ArrayList<>(nodes), timeout, req.getKey(), req.getId());
        req.getPeer().sendMessage(msg);
    }

    private long searchingTimeout(ChunkRequest chunkRequest, BzzRetrieveReqMessage req) {
        // TODO
        return 0;
    }

    private enum EntryReqStatus {
        Searching,
        Found
    }

    private class ChunkRequest {
        EntryReqStatus status = EntryReqStatus.Searching;
        Map<Long, Collection<BzzRetrieveReqMessage>> requesters = new HashMap<>();
        List<CompletableFuture<Chunk>> localRequesters = new ArrayList<>();
    }

    // Statistics gathers
    public final Statter statInMsg = Statter.create("net.swarm.bzz.inMessages");
    public final Statter statOutMsg = Statter.create("net.swarm.bzz.outMessages");
    public final Statter statHandshakes = Statter.create("net.swarm.bzz.handshakes");

    public final Statter statInStoreReq = Statter.create("net.swarm.in.storeReq");
    public final Statter statInGetReq = Statter.create("net.swarm.in.getReq");
    public final Statter statOutStoreReq = Statter.create("net.swarm.out.storeReq");
    public final Statter statOutGetReq = Statter.create("net.swarm.out.getReq");
}


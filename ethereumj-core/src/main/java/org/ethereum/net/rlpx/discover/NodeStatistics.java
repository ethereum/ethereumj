package org.ethereum.net.rlpx.discover;

import org.ethereum.net.client.Capability;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.swarm.Statter;
import org.ethereum.util.ByteUtil;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Handles all possible statistics related to a Node
 * The primary aim of this is collecting info about a Node
 * for maintaining its reputation.
 *
 * Created by Anton Nashatyrev on 16.07.2015.
 */
public class NodeStatistics {
    public final static int REPUTATION_PREDEFINED = 1000500;
    public final static int REPUTATION_HANDSHAKE = 3000;
    public final static int REPUTATION_AUTH = 1000;
    public final static int REPUTATION_DISCOVER_PING = 1;
    public final static long TOO_MANY_PEERS_PENALIZE_TIMEOUT = 10 * 1000;

    public class StatHandler {
        AtomicLong count = new AtomicLong(0);
        public void add() {count.incrementAndGet(); }
        public void add(long delta) {count.addAndGet(delta); }
        public long get() {return count.get();}
        public String toString() {return count.toString();}
    }

    static class Persistent  implements Serializable {
        private static final long serialVersionUID = -1246930309060559921L;
        static final Serializer<Persistent> MapDBSerializer = new Serializer<Persistent>() {
            @Override
            public void serialize(DataOutput out, Persistent value) throws IOException {
                out.writeInt(value.reputation);
            }

            @Override
            public Persistent deserialize(DataInput in, int available) throws IOException {
                Persistent persistent = new Persistent();
                persistent.reputation = in.readInt();
                return persistent;
            }
        };
        int reputation;
    }

    private final Node node;

    private boolean isPredefined = false;

    private int savedReputation = 0;

    // discovery stat
    public final StatHandler discoverOutPing = new StatHandler();
    public final StatHandler discoverInPong = new StatHandler();
    public final StatHandler discoverOutPong = new StatHandler();
    public final StatHandler discoverInPing = new StatHandler();
    public final StatHandler discoverInFind = new StatHandler();
    public final StatHandler discoverOutFind = new StatHandler();
    public final StatHandler discoverInNeighbours = new StatHandler();
    public final StatHandler discoverOutNeighbours = new StatHandler();
    public final Statter.SimpleStatter discoverMessageLatency;
    public final AtomicLong lastPongReplyTime = new AtomicLong(0l); // in milliseconds

    // rlpx stat
    public final StatHandler rlpxConnectionAttempts = new StatHandler();
    public final StatHandler rlpxAuthMessagesSent = new StatHandler();
    public final StatHandler rlpxOutHello = new StatHandler();
    public final StatHandler rlpxInHello = new StatHandler();
    public final StatHandler rlpxHandshake = new StatHandler();
    public final StatHandler rlpxOutMessages = new StatHandler();
    public final StatHandler rlpxInMessages = new StatHandler();
    // Not the fork we are working on
    // Set only after specific block hashes received
    public boolean wrongFork;

    private String clientId = "";

    public final List<Capability> capabilities = new ArrayList<>();

    private ReasonCode rlpxLastRemoteDisconnectReason = null;
    private ReasonCode rlpxLastLocalDisconnectReason = null;
    private long lastDisconnectedTime = 0;

    // Eth stat
    public final StatHandler ethHandshake = new StatHandler();
    public final StatHandler ethInbound = new StatHandler();
    public final StatHandler ethOutbound = new StatHandler();
    private StatusMessage ethLastInboundStatusMsg = null;
    private BigInteger ethTotalDifficulty = BigInteger.ZERO;

    // Eth63 stat
    public final StatHandler eth63NodesRequested = new StatHandler();
    public final StatHandler eth63NodesReceived = new StatHandler();
    public final StatHandler eth63NodesRetrieveTime = new StatHandler();

    // Par stat
    public final StatHandler parInbound = new StatHandler();
    public final StatHandler parOutbound = new StatHandler();

    // Par1 stat
    public final StatHandler par1ChunksRequested = new StatHandler();
    public final StatHandler par1ChunksReceived = new StatHandler();
    public final StatHandler par1ChunkBytesReceived = new StatHandler();
    public final StatHandler par1ChunksRetrieveTime = new StatHandler();

    public NodeStatistics(Node node) {
        this.node = node;
        discoverMessageLatency = (Statter.SimpleStatter) Statter.create(getStatName() + ".discoverMessageLatency");
    }

    private int getSessionReputation() {
        return getSessionFairReputation() + (isPredefined ? REPUTATION_PREDEFINED : 0);
    }
    private int getSessionFairReputation() {
        int discoverReput = 0;

        discoverReput += min(discoverInPong.get(), 10) * (discoverOutPing.get() == discoverInPong.get() ? 2 : 1);
        discoverReput += min(discoverInNeighbours.get(), 10) * 2;
//        discoverReput += 20 / (min((int)discoverMessageLatency.getAvrg(), 1) / 100);

        int rlpxReput = 0;
        rlpxReput += rlpxAuthMessagesSent.get() > 0 ? 10 : 0;
        rlpxReput += rlpxHandshake.get() > 0 ? 20 : 0;
        rlpxReput += min(rlpxInMessages.get(), 10) * 3;

        if (wasDisconnected()) {
            if (rlpxLastLocalDisconnectReason == null && rlpxLastRemoteDisconnectReason == null) {
                // means connection was dropped without reporting any reason - bad
                rlpxReput *= 0.3;
            } else if (rlpxLastLocalDisconnectReason != ReasonCode.REQUESTED) {
                // the disconnect was not initiated by discover mode
                if (rlpxLastRemoteDisconnectReason == ReasonCode.TOO_MANY_PEERS) {
                    // The peer is popular, but we were unlucky
                    rlpxReput *= 0.3;
                } else if (rlpxLastRemoteDisconnectReason != ReasonCode.REQUESTED) {
                    // other disconnect reasons
                    rlpxReput *= 0.2;
                }
            }
        }

        return discoverReput + 100 * rlpxReput;
    }

    public int getReputation() {
        return isReputationPenalized() ? 0 : savedReputation / 2 + getSessionReputation();
    }

    private boolean isReputationPenalized() {
        if (wrongFork) return true;
        if (wasDisconnected() && rlpxLastRemoteDisconnectReason == ReasonCode.TOO_MANY_PEERS &&
                System.currentTimeMillis() - lastDisconnectedTime < TOO_MANY_PEERS_PENALIZE_TIMEOUT) {
            return true;
        }
        if (wasDisconnected() && rlpxLastRemoteDisconnectReason == ReasonCode.DUPLICATE_PEER &&
                System.currentTimeMillis() - lastDisconnectedTime < TOO_MANY_PEERS_PENALIZE_TIMEOUT) {
            return true;
        }
        return  rlpxLastLocalDisconnectReason == ReasonCode.NULL_IDENTITY ||
                rlpxLastRemoteDisconnectReason == ReasonCode.NULL_IDENTITY ||
                rlpxLastLocalDisconnectReason == ReasonCode.INCOMPATIBLE_PROTOCOL ||
                rlpxLastRemoteDisconnectReason == ReasonCode.INCOMPATIBLE_PROTOCOL ||
                rlpxLastLocalDisconnectReason == ReasonCode.USELESS_PEER ||
                rlpxLastRemoteDisconnectReason == ReasonCode.USELESS_PEER;
    }

    public void nodeDisconnectedRemote(ReasonCode reason) {
        lastDisconnectedTime = System.currentTimeMillis();
        rlpxLastRemoteDisconnectReason = reason;
    }

    public void nodeDisconnectedLocal(ReasonCode reason) {
        lastDisconnectedTime = System.currentTimeMillis();
        rlpxLastLocalDisconnectReason = reason;
    }

    public void disconnected() {
        lastDisconnectedTime = System.currentTimeMillis();
    }

    public boolean wasDisconnected() {
        return lastDisconnectedTime > 0;
    }


    public void ethHandshake(StatusMessage ethInboundStatus) {
        this.ethLastInboundStatusMsg = ethInboundStatus;
        this.ethTotalDifficulty = ethInboundStatus.getTotalDifficultyAsBigInt();
        ethHandshake.add();
    }

    public BigInteger getEthTotalDifficulty() {
        return ethTotalDifficulty;
    }

    public void setEthTotalDifficulty(BigInteger ethTotalDifficulty) {
        this.ethTotalDifficulty = ethTotalDifficulty;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setPredefined(boolean isPredefined) {
        this.isPredefined = isPredefined;
    }

    public boolean isPredefined() {
        return isPredefined;
    }

    public StatusMessage getEthLastInboundStatusMsg() {
        return ethLastInboundStatusMsg;
    }

    private String getStatName() {
        return "ethj.discover.nodes." + node.getHost() + ":" + node.getPort();
    }

    Persistent getPersistent() {
        Persistent persistent = new Persistent();
        persistent.reputation = isReputationPenalized() ? 0 : (savedReputation + getSessionFairReputation()) / 2;
        return persistent;
    }

    void setPersistedData(Persistent persistedData) {
        savedReputation = persistedData.reputation;
    }

    @Override
    public String toString() {
        return "NodeStat[reput: " + getReputation() + "(" + savedReputation + "), discover: " +
                discoverInPong + "/" + discoverOutPing + " " +
                discoverOutPong + "/" + discoverInPing + " " +
                discoverInNeighbours + "/" + discoverOutFind + " " +
                discoverOutNeighbours + "/" + discoverInFind + " " +
                ((int)discoverMessageLatency.getAvrg()) + "ms" +
                ", rlpx: " + rlpxHandshake + "/" + rlpxAuthMessagesSent + "/" + rlpxConnectionAttempts + " " +
                rlpxInMessages + "/" + rlpxOutMessages +
                ", eth: " + ethHandshake + "/" + ethInbound + "/" + ethOutbound + " " +
                (ethLastInboundStatusMsg != null ? ByteUtil.toHexString(ethLastInboundStatusMsg.getTotalDifficulty()) : "-") + " " +
                (wasDisconnected() ? "X " : "") +
                (rlpxLastLocalDisconnectReason != null ? ("<=" + rlpxLastLocalDisconnectReason) : " ") +
                (rlpxLastRemoteDisconnectReason != null ? ("=>" + rlpxLastRemoteDisconnectReason) : " ")  +
                "[" + clientId + "]";
    }


}

package org.ethereum.net.rlpx.discover;

import org.ethereum.net.rlpx.*;
import org.ethereum.net.swarm.Util;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The instance of this class responsible for discovery messages exchange with the specified Node
 * It also manages itself regarding inclusion/eviction from Kademlia table
 *
 * Created by Anton Nashatyrev on 14.07.2015.
 */
public class NodeHandler {
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    static Queue<NodeHandler> aliveNodes = new ArrayDeque<>();
    private static ScheduledExecutorService pongTimer = Executors.newSingleThreadScheduledExecutor();
    static long PingTimeout = 15000; //KademliaOptions.REQ_TIMEOUT;


    public enum State {
        /**
         * The new node was just discovered either by receiving it with Neighbours
         * message or by receiving Ping from a new node
         * In either case we are sending Ping and waiting for Pong
         * If the Pong is received the node becomes {@link #Alive}
         * If the Pong was timed out the node becomes {@link #Dead}
         */
        Discovered,
        /**
         * The node didn't send the Pong message back withing acceptable timeout
         * This is the final state
         */
        Dead,
        /**
         * The node responded with Pong and is now the candidate for inclusion to the table
         * If the table has bucket space for this node it is added to table and becomes {@link #Active}
         * If the table bucket is full this node is challenging with the old node from the bucket
         *     if it wins then old node is dropped, and this node is added and becomes {@link #Active}
         *     else this node becomes {@link #NonActive}
         */
        Alive,
        /**
         * The node is included in the table. It may become {@link #EvictCandidate} if a new node
         * wants to become Active but the table bucket is full.
         */
        Active,
        /**
         * This node is in the table but is currently challenging with a new Node candidate
         * to survive in the table bucket
         * If it wins then returns back to {@link #Active} state, else is evicted from the table
         * and becomes {@link #NonActive}
         */
        EvictCandidate,
        /**
         * Veteran. It was Alive and even Active but is now retired due to loosing the challenge
         * with another Node.
         * For no this is the final state
         * It's an option for future to return veterans back to the table
         */
        NonActive
    }

    Node node;
    NodeManager nodeManager;
    private NodeStatistics nodeStatistics;

    State state;
    boolean waitForPong = false;
    long pingSent;
    int pingTrials = 3;
    NodeHandler replaceCandidate;

    public NodeHandler(Node node, NodeManager nodeManager) {
        this.node = node;
        this.nodeManager = nodeManager;
        changeState(State.Discovered);
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(node.getHost(), node.getPort());
    }

    public Node getNode() {
        return node;
    }

    public State getState() {
        return state;
    }

    public NodeStatistics getNodeStatistics() {
        if (nodeStatistics == null) {
            nodeStatistics = new NodeStatistics(node);
        }
        return nodeStatistics;
    }

    private void challengeWith(NodeHandler replaceCandidate) {
        this.replaceCandidate = replaceCandidate;
        changeState(State.EvictCandidate);
    }

    // Manages state transfers
    private void changeState(State newState) {
        State oldState = state;
        if (newState == State.Discovered) {
            // will wait for Pong to assume this alive
            sendPing();
        }
        if (newState == State.Alive) {
            Node evictCandidate = nodeManager.table.addNode(this.node);
            if (evictCandidate == null) {
                newState = State.Active;
            } else {
                NodeHandler evictHandler = nodeManager.getNodeHandler(evictCandidate);
                if (evictHandler.state == State.EvictCandidate) {
                    // already challenging for eviction
                    // adding to alive for later challenges
                    aliveNodes.add(this);
                } else {
                    evictHandler.challengeWith(this);
                }
            }
        }
        if (newState == State.Active) {
            if (oldState == State.Alive) {
                // new node won the challenge
                nodeManager.table.addNode(node);
            } else if (oldState == State.EvictCandidate) {
                // nothing to do here the node is already in the table
            } else {
                // wrong state transition
            }
        }
        if (newState == State.NonActive) {
            if (oldState == State.EvictCandidate) {
                // lost the challenge
                // Removing ourselves from the table
                nodeManager.table.dropNode(node);
                // Congratulate the winner
                replaceCandidate.changeState(State.Active);
            } else if (oldState == State.Alive) {
                // ok the old node was better, nothing to do here
            } else {
                // wrong state transition
            }
        }

        if (newState == State.EvictCandidate) {
            // trying to survive, sending ping and waiting for pong
            sendPing();
        }
        state = newState;
        stateChanged(oldState, newState);
    }

    protected void stateChanged(State oldState, State newState) {
        logger.debug("State change " + oldState + " -> " + newState + ": " + this);
        nodeManager.stateChanged(this, oldState, newState);
    }

    void handlePing(PingMessage msg) {
        logger.debug(" ===> [PING] " + this);
        getNodeStatistics().discoverInPing.add();
        if (!nodeManager.table.getNode().equals(node)) {
            sendPong(msg.getMdc());
        }
    }

    void handlePong(PongMessage msg) {
        logger.debug(" ===> [PONG] " + this);
        if (waitForPong) {
            waitForPong = false;
            getNodeStatistics().discoverInPong.add();
            getNodeStatistics().discoverMessageLatency.add(Util.curTime() - pingSent);
            changeState(State.Alive);
        }
    }

    void handleNeighbours(NeighborsMessage msg) {
        logger.debug(" ===> [NEIGHBOURS] " + this + ", Count: " + msg.getNodes().size());
        getNodeStatistics().discoverInNeighbours.add();
        for (Node n : msg.getNodes()) {
            nodeManager.getNodeHandler(n);
        }
    }

    void handleFindNode(FindNodeMessage msg) {
        logger.debug(" ===> [FIND_NODE] " + this);
        getNodeStatistics().discoverInFind.add();
        List<Node> closest = nodeManager.table.getClosestNodes(msg.getTarget());
        sendNeighbours(closest);
    }

    void handleTimedOut() {
        waitForPong = false;
        if (--pingTrials > 0) {
            sendPing();
        } else {
            if (state == State.Discovered) {
                changeState(State.Dead);
            } else if (state == State.EvictCandidate) {
                changeState(State.NonActive);
            } else {
                // TODO just influence to reputation
            }
        }
    }

    void sendPing() {
        if (waitForPong) {
            logger.debug("<=/=  [PING] (Waiting for pong) " + this);
        }
        logger.debug("<===  [PING] " + this);

        Message ping = PingMessage.create(nodeManager.table.getNode().getHost(),
                nodeManager.table.getNode().getPort(), nodeManager.key);
        waitForPong = true;
        pingSent = Util.curTime();
        sendMessage(ping);
        getNodeStatistics().discoverOutPing.add();

        pongTimer.schedule(new Runnable() {
            public void run() {
                if (waitForPong) {
                    waitForPong = false;
                    handleTimedOut();
                }
            }
        }, PingTimeout, TimeUnit.MILLISECONDS);
    }

    void sendPong(byte[] mdc) {
        logger.debug("<===  [PONG] " + this);
        Message pong = PongMessage.create(mdc, node.getHost(), node.getPort(), nodeManager.key);
        sendMessage(pong);
        getNodeStatistics().discoverOutPong.add();
    }

    void sendNeighbours(List<Node> neighbours) {
        logger.debug("<===  [NEIGHBOURS] " + this);
        NeighborsMessage neighbors = NeighborsMessage.create(neighbours, nodeManager.key);
        sendMessage(neighbors);
        getNodeStatistics().discoverOutNeighbours.add();
    }

    void sendFindNode(byte[] target) {
        logger.debug("<===  [FIND_NODE] " + this);
        Message findNode = FindNodeMessage.create(target, nodeManager.key);
        sendMessage(findNode);
        getNodeStatistics().discoverOutFind.add();
    }

    private void sendMessage(Message msg) {
        nodeManager.sendOutbound(new DiscoveryEvent(msg, getInetSocketAddress()));
    }

    @Override
    public String toString() {
        return "NodeHandler[state: " + state + ", node: " + node.getHost() + ":" + node.getPort() + ", id="
                + (node.getId().length > 0 ? Hex.toHexString(node.getId(), 0, 4) : "empty") + "]";
    }


}

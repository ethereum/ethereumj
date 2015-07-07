package org.ethereum.net.swarm.bzz;

import org.ethereum.net.client.Capability;
import org.ethereum.net.swarm.Key;
import org.ethereum.net.swarm.NetStore;
import org.ethereum.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The class is the lowest level right above the network layer.
 * Responsible for BZZ handshaking, brokering inbound messages
 * and delivering outbound messages.
 *
 * Created by Anton Nashatyrev on 18.06.2015.
 */
public class BzzProtocol implements Functional.Consumer<BzzMessage> {
    private final static Logger LOG = LoggerFactory.getLogger("net.bzz");

    private final static AtomicLong idGenerator = new AtomicLong(0);

    public final static int Version            = 0;
    public final static long ProtocolLength     = 8;
    public final static long ProtocolMaxMsgSize = 10 * 1024 * 1024;
    public final static int NetworkId          = 0;
    public final static int Strategy           = 0;

    private NetStore netStore;
    private Functional.Consumer<BzzMessage> messageSender;
    private PeerAddress node;

    private boolean handshaken = false;
    private boolean handshakeOut = false;
    private List<BzzMessage> pendingHandshakeOutMessages = new ArrayList<>();
    private List<BzzMessage> pendingHandshakeInMessages = new ArrayList<>();

    public BzzProtocol(NetStore netStore) {
        this.netStore = netStore;
    }

    /**
     * Installs the message sender.
     * Normally this is BzzHandler which just sends the message to the peer over the wire
     * In the testing environment this could be a special handler which delivers the message
     * without network stack
     */
    public void setMessageSender(Functional.Consumer<BzzMessage> messageSender) {
        this.messageSender = messageSender;
    }

    public void start() {
        handshakeOut();
    }

    /**
     * Gets the address of the Peer connected to this instance.
     */
    public PeerAddress getNode() {
        return node;
    }

    /**
     * Sends the Status message to the peer
     */
    private void handshakeOut() {
        if (!handshakeOut) {
            handshakeOut = true;
            BzzStatusMessage outStatus = new BzzStatusMessage(Version, "honey",
                    netStore.getSelfAddress(), NetworkId,
                    Collections.singletonList(new Capability(Capability.BZZ, (byte) 0)));
            sendMessageImpl(outStatus);
        }
    }

    /**
     * Handles inbound Status Message
     */
    private void handshakeIn(BzzStatusMessage msg) {
        if (!handshaken) {
            netStore.statHandshakes.add(1);
            // TODO check status parameters
            node = msg.getAddr();
            netStore.getHive().addPeer(this);

            handshaken = true;
            handshakeOut();

            start();

            for (BzzMessage pmsg : pendingHandshakeOutMessages) {
                sendMessageImpl(pmsg);
            }
            pendingHandshakeOutMessages = null;
            // ping the peer for self neighbours
            sendMessageImpl(new BzzRetrieveReqMessage(Key.zeroKey()));

            for (BzzMessage pmsg : pendingHandshakeInMessages) {
                handleMsg(pmsg);
            }
            pendingHandshakeInMessages = null;
        }
    }

    public synchronized void sendMessage(BzzMessage msg) {
        if (handshaken) {
            sendMessageImpl(msg);
        } else {
            pendingHandshakeOutMessages.add(msg);
        }
    }
    private void sendMessageImpl(BzzMessage msg) {
        netStore.statOutMsg.add(1);
        msg.setId(idGenerator.incrementAndGet());
        messageSender.accept(msg);
    }

    @Override
    public void accept(BzzMessage bzzMessage) {
        handleMsg(bzzMessage);
    }

    private void handleMsg(BzzMessage msg) {
        synchronized (netStore) {
            netStore.statInMsg.add(1);
            msg.setPeer(this);
            LOG.trace(node + " ===> " + netStore.getSelfAddress() + ": " + msg);
            if (msg.getCommand() == BzzMessageCodes.STATUS) {
                handshakeIn((BzzStatusMessage) msg);
            } else {
                if (!handshaken) {
                    pendingHandshakeInMessages.add(msg);
                } else {
                    switch (msg.getCommand()) {
                        case STORE_REQUEST:
                            netStore.addStoreRequest((BzzStoreReqMessage) msg);
                            break;
                        case RETRIEVE_REQUEST:
                            netStore.addRetrieveRequest((BzzRetrieveReqMessage) msg);
                            break;
                        case PEERS:
                            netStore.getHive().addPeerRecords((BzzPeersMessage) msg);
                            break;
                        default:
                            throw new RuntimeException("Invalid BZZ command: " + msg.getCommand() + ": " + msg);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return netStore.getSelfAddress() + " => " + node + " (Node: " + node + ")";
    }
}

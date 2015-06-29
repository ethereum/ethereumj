package org.ethereum.net.swarm.bzz;

import org.ethereum.net.eth.EthMessageCodes;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.Peer;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.swarm.Util;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public abstract class BzzMessage extends Message {

    // non-null for incoming messages
    BzzProtocol peer;

    public BzzMessage() {
    }

    public BzzMessage(byte[] encoded) {
        super(encoded);
        decode();
    }

    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.fromByte(code);
    }

    protected abstract void decode();

    public BzzProtocol getPeer() {
        return peer;
    }

    public void setPeer(BzzProtocol peer) {
        this.peer = peer;
    }
}

package org.ethereum.net.swarm.bzz;

import org.ethereum.net.swarm.Key;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.util.ArrayList;
import java.util.List;

public class BzzPeersMessage extends BzzMessage {

    private List<PeerAddress> peers;
    // optional
    private Key key;
    private long id;

    public BzzPeersMessage(byte[] encoded) {
        super(encoded);
    }

    public BzzPeersMessage(List<PeerAddress> peers, Key key, long id) {
        this.peers = peers;
        this.key = key;
        this.id = id;
    }

    public BzzPeersMessage(List<PeerAddress> peers) {
        this.peers = peers;
    }

    @Override
    protected void decode() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        peers = new ArrayList<>();
        RLPList addrs = (RLPList) paramsList.get(0);
        for (RLPElement a : addrs) {
            peers.add(PeerAddress.parse((RLPList) a));
        }
        if (paramsList.size() > 1) {
            key = new Key(paramsList.get(1).getRLPData());
        }
        if (paramsList.size() > 2) {
            id = ByteUtil.byteArrayToLong(paramsList.get(2).getRLPData());;
        }

        parsed = true;
    }

    private void encode() {
        byte[][] bPeers = new byte[this.peers.size()][];
        for (int i = 0; i < this.peers.size(); i++) {
            PeerAddress peer = this.peers.get(i);
            bPeers[i] = peer.encodeRlp();
        }
        byte[] bPeersList = RLP.encodeList(bPeers);

        if (key == null) {
            this.encoded = RLP.encodeList(bPeersList);
        } else {
            this.encoded = RLP.encodeList(bPeersList,
                    RLP.encodeElement(key.getBytes()),
                    RLP.encodeElement(ByteUtil.longToBytes(id)));
        }
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public List<PeerAddress> getPeers() {
        return peers;
    }

    public Key getKey() {
        return key;
    }

    public long getId() {
        return id;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public BzzMessageCodes getCommand() {
        return BzzMessageCodes.PEERS;
    }

    @Override
    public String toString() {
        return "BzzPeersMessage{" +
                "peers=" + peers +
                ", key=" + key +
                ", id=" + id +
                '}';
    }
}

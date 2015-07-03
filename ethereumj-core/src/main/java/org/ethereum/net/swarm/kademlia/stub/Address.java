package org.ethereum.net.swarm.kademlia.stub;

import org.ethereum.crypto.SHA3Helper;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.swarm.Key;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Admin on 18.06.2015.
 */
public class Address extends Key {
    public Address(PeerInfo peer) {
        this(Hex.decode(peer.getPeerId()));
    }
    public Address(byte[] id) {
        super(SHA3Helper.sha3(id));
    }
}

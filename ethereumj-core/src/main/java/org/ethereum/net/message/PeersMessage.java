package org.ethereum.net.message;

import static org.ethereum.net.Command.PEERS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ethereum.net.Command;
import org.ethereum.net.client.Peer;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class PeersMessage extends Message {

    private boolean parsed = false;

    private Set<Peer> peers = new LinkedHashSet<>();

    public PeersMessage(byte[] payload) {
        super(payload);
    }
    
    public PeersMessage(Set<Peer> peers) {
        this.peers = peers;
        this.parsed = true;
    }

    public void parse() {

		RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        if ( (((RLPItem)(paramsList).get(0)).getRLPData()[0] & 0xFF) != PEERS.asByte())
            throw new RuntimeException("Not a PeersMessage command");

        for (int i = 1; i < paramsList.size(); ++i) {

			RLPList peerParams = (RLPList) paramsList.get(i);
			byte[] ip 			= ((RLPItem) peerParams.get(0)).getRLPData();
			byte[] shortData 	= ((RLPItem) peerParams.get(1)).getRLPData();
            short peerPort          = 0;
            if (shortData.length == 1)
                peerPort = shortData[0];
            else {
                ByteBuffer bb = ByteBuffer.wrap(shortData, 0, shortData.length);
                peerPort = bb.getShort();
            }
            byte[] peerId           = ((RLPItem) peerParams.get(2)).getRLPData();
            Peer peer = new Peer(ip, peerPort, peerId);
            peers.add(peer);
        }
        this.parsed = true;
    }
    
    @Override
    public Command getCommand() {
    	return PEERS;
    }

    @Override
    public byte[] getEncoded() {
    	if (encoded == null) this.encode();
        return encoded;
    }
    
    private void encode() {
    	byte[][] encodedByteArrays = new byte[this.peers.size()+1][];
    	encodedByteArrays[0] = RLP.encodeByte(this.getCommand().asByte());
    	List<Peer> peerList = new ArrayList<>();
    	peerList.addAll(this.peers);
    	for (int i = 0; i < peerList.size(); i++) {
    		encodedByteArrays[i+1] = peerList.get(i).getEncoded();
		}
    	this.encoded = RLP.encodeList(encodedByteArrays);
    }

    public Set<Peer> getPeers() {
        if (!parsed) this.parse();
        return peers;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        if (!parsed) this.parse();
        
        StringBuffer sb = new StringBuffer();
		for (Peer peerData : peers) {
            sb.append("\n       ").append(peerData);
        }
        return "[command=" + this.getCommand().name() + sb.toString() + "]";
    }
}
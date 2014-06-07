package org.ethereum.net.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.net.Command.PEERS;

import org.ethereum.net.Command;
import org.ethereum.net.client.PeerData;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class PeersMessage extends Message {

    RLPList rawData;
    boolean parsed = false;

    List<PeerData> peers = new ArrayList<PeerData>();

    public PeersMessage(RLPList rawData) {
        this.rawData = rawData;
        parsed = false;
    }

    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.get(0);

        if (Command.fromInt(((RLPItem)(paramsList).get(0)).getRLPData()[0] & 0xFF) != PEERS){
            throw new Error("PeersMessage: parsing for mal data");
        }

        for (int i = 1; i < paramsList.size(); ++i){

            RLPList peerParams = (RLPList)paramsList.get(i);
            byte[] ip = ((RLPItem) peerParams.get(0)).getRLPData();
            byte[] shortData = ((RLPItem) peerParams.get(1)).getRLPData();
            short peerPort          = 0;
            if (shortData.length == 1)
                peerPort = shortData[0];
            else {
                ByteBuffer bb = ByteBuffer.wrap(shortData, 0, shortData.length);
                peerPort = bb.getShort();
            }
            byte[] peerId           = ((RLPItem) peerParams.get(2)).getRLPData();
            PeerData peer = new PeerData(ip, peerPort, peerId);
            peers.add(peer);
        }
        this.parsed = true;
        // todo: what to do when mal data ?
    }

    @Override
    public byte[] getPayload() {
        return null;
    }

    public List<PeerData> getPeers() {
        if (!parsed)
            parseRLP();
        return peers;
    }

    public String toString(){
        if (!parsed)
            parseRLP();
        
        StringBuffer sb = new StringBuffer();
		for (PeerData peerData : peers) {
            sb.append("[").append(peerData).append("] \n   ");
        }
        return "Peers Message [\n   " + sb.toString() + "]";
    }
}
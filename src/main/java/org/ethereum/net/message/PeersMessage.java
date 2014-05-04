package org.ethereum.net.message;

import org.ethereum.net.RLP;
import org.ethereum.net.message.Message;
import org.ethereum.net.rlp.RLPItem;
import org.ethereum.net.rlp.RLPList;
import org.ethereum.net.vo.PeerData;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 06/04/14 14:56
 */
public class PeersMessage extends Message {

    private final byte commandCode = 0x11;

    RLPList rawData;
    boolean parsed = false;


    List<PeerData> peers = new ArrayList<PeerData>();

    public PeersMessage(){}

    public PeersMessage(RLPList rawData) {
        this.rawData = rawData;
        parsed = false;
    }


    @Override
    public void parseRLP() {

        RLPList paramsList = (RLPList) rawData.getElement(0);

        if ((((RLPItem)(paramsList).getElement(0)).getData()[0] & 0xFF) != commandCode){

            throw new Error("PeersMessage: parsing for mal data");
        }


        for (int i = 1; i < paramsList.size(); ++i){

            RLPList peerParams = (RLPList)paramsList.getElement(i);

            byte[] ip = ((RLPItem) peerParams.getElement(0)).getData();

            byte[] shortData = ((RLPItem) peerParams.getElement(1)).getData();

            short peerPort          = 0;
            if (shortData.length == 1)

                peerPort = shortData[0];
            else{

                ByteBuffer bb = ByteBuffer.wrap(shortData, 0, shortData.length);
                peerPort = bb.getShort();
            }

            byte[] peerId           = ((RLPItem) peerParams.getElement(2)).getData();

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

        if (!parsed){
            parseRLP();
        }
        return peers;
    }

    public String toString(){

        if (!parsed){
            parseRLP();
        }

        StringBuffer sb = new StringBuffer();

        for (PeerData peerData : peers){

            sb.append("[").append(peerData).append("] \n   ");
        }

        return "Peers Message [\n   " + sb.toString() + "]";

    }
}

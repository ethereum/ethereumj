package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.Iterator;

import org.ethereum.net.client.PeerData;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class PeersMessageTest {

    /* PEERS */

    @Test /*  PeersMessage 1*/
    public void test_5() {

        String peersMessageRaw = "F89911F84A84364845B482765FB840430BB21ECF73A54AEF511404948C016532406B371EDABD20A478C3ECD22052A0065A7399A6D19594E24B153930E63A3B12B3BA4F30A3CDA1977D60D4060FFF25F84A845163E11282765FB8405F1DBE5E50E92A6B67377E079D986155C0D82D964E65332F524810ED7831A52837F1C0FB042EA2A25548E3B444C337F54C7547B2D877734E2899F40BFA23ED51";
        byte[] payload = Hex.decode(peersMessageRaw);
        RLPList rlpList = RLP.decode2(payload);

        PeersMessage peersMessage= new PeersMessage(rlpList);
        System.out.println(peersMessage);

        assertEquals(2, peersMessage.getPeers().size());

        Iterator<PeerData> it = peersMessage.getPeers().iterator(); it.next();
        PeerData peerData = it.next();

        assertEquals("/81.99.225.18", peerData.getInetAddress().toString());
        assertEquals(30303, peerData.getPort());
        assertEquals("5F1DBE5E50E92A6B67377E079D986155C0D82D964E65332F524810ED7831A52837F1C0FB042EA2A25548E3B444C337F54C7547B2D877734E2899F40BFA23ED51",
        		Hex.toHexString( peerData.getPeerId() ).toUpperCase());
    }


    @Test /* Peers msg parsing performance*/
    public void test_7() throws UnknownHostException {

        long time1 = System.currentTimeMillis();
        for (int i = 0; i < 20000; ++i) {

            String peersPacketRaw = "F89911F84A84364845B482765FB840430BB21ECF73A54AEF511404948C016532406B371EDABD20A478C3ECD22052A0065A7399A6D19594E24B153930E63A3B12B3BA4F30A3CDA1977D60D4060FFF25F84A845163E11282765FB8405F1DBE5E50E92A6B67377E079D986155C0D82D964E65332F524810ED7831A52837F1C0FB042EA2A25548E3B444C337F54C7547B2D877734E2899F40BFA23ED51";

            byte[] payload = Hex.decode(peersPacketRaw);
            RLPList rlpList = RLP.decode2(payload);

            PeersMessage peersMessage = new PeersMessage(rlpList);
            peersMessage.parseRLP();
        }
        long time2 = System.currentTimeMillis();

        System.out.println("20,000 PEERS packets parsing: " + (time2 - time1) + "(msec)");
    }
}


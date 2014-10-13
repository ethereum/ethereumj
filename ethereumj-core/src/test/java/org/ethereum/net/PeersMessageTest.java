package org.ethereum.net;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ethereum.net.message.Command;
import org.ethereum.net.message.GetPeersMessage;
import org.ethereum.net.message.PeersMessage;
import org.ethereum.net.peerdiscovery.PeerData;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class PeersMessageTest {

    /* GET_PEERS */

    @Test /* GetPeersMessage */
    public void testGetPeers() {

        GetPeersMessage getPeersMessage = new GetPeersMessage();
        System.out.println(getPeersMessage);

        assertEquals(Command.GET_PEERS, getPeersMessage.getCommand());
        assertEquals(PeersMessage.class, getPeersMessage.getAnswerMessage());
    }
	
    /* PEERS */

    @Test /* PeersMessage 1 from RLP */
    public void testPeers_1() {

        String peersMessageRaw = "F8 99 05 F8 4A 84 36 48 45 B4 82 76 "
        		+ "5F B8 40 43 0B B2 1E CF 73 A5 4A EF 51 14 04 94 8C "
        		+ "01 65 32 40 6B 37 1E DA BD 20 A4 78 C3 EC D2 20 52 "
        		+ "A0 06 5A 73 99 A6 D1 95 94 E2 4B 15 39 30 E6 3A 3B "
        		+ "12 B3 BA 4F 30 A3 CD A1 97 7D 60 D4 06 0F FF 25 F8 "
        		+ "4A 84 51 63 E1 12 82 76 5F B8 40 5F 1D BE 5E 50 E9 "
        		+ "2A 6B 67 37 7E 07 9D 98 61 55 C0 D8 2D 96 4E 65 33 "
        		+ "2F 52 48 10 ED 78 31 A5 28 37 F1 C0 FB 04 2E A2 A2 "
        		+ "55 48 E3 B4 44 C3 37 F5 4C 75 47 B2 D8 77 73 4E 28 "
        		+ "99 F4 0B FA 23 ED 51";
        byte[] payload = Hex.decode(peersMessageRaw);

        PeersMessage peersMessage= new PeersMessage(payload);
        System.out.println(peersMessage);

        assertEquals(2, peersMessage.getPeers().size());

        Iterator<PeerData> it = peersMessage.getPeers().iterator(); it.next();
        PeerData peer = it.next();

        assertEquals(Command.PEERS, peersMessage.getCommand());
        assertEquals("/81.99.225.18", peer.getAddress().toString());
        assertEquals(30303, peer.getPort());
        assertEquals("5F1DBE5E50E92A6B67377E079D986155C0D82D964E65332F524810ED7831A52837F1C0FB042EA2A25548E3B444C337F54C7547B2D877734E2899F40BFA23ED51",
        		peer.getPeerId().toUpperCase());
    }

    @Test /*  PeersMessage 2 from constructor */
    public void testPeers_2() throws UnknownHostException {
    	Set<PeerData> peers = new HashSet<>();
    	peers.add(new PeerData(InetAddress.getByName("82.217.72.169"), 30303, "585764a3c49a3838c69ad0855abfeb5672f71b072af62082b5679961781100814b8de88a8fbc1da7c73791f88159d73b5d2a13a5579535d603e045c3db5cbb75"));
    	peers.add(new PeerData(InetAddress.getByName("192.168.1.193"), 30303, ""));
        PeersMessage peersMessage = new PeersMessage(peers);
        System.out.println(peersMessage.toString());

        String expected = "f85905f84b8452d948a982765fb840585764a3c49a3838c69ad0855abfeb5672f71b072af62082b5679961781100814b8de88a8fbc1da7c73791f88159d73b5d2a13a5579535d603e045c3db5cbb75c0ca84c0a801c182765f80c0";
        assertEquals(Command.PEERS, peersMessage.getCommand());
        assertEquals(expected, Hex.toHexString(peersMessage.getEncoded()));
    }
    
    @Test /* Peers msg parsing performance */
    public void testPeersPerformance() throws UnknownHostException {

        long time1 = System.currentTimeMillis();
        for (int i = 0; i < 20000; ++i) {

            String peersPacketRaw = "F8 99 05 F8 4A 84 36 48 45 B4 82 76 "
            		+ "5F B8 40 43 0B B2 1E CF 73 A5 4A EF 51 14 04 94 "
            		+ "8C 01 65 32 40 6B 37 1E DA BD 20 A4 78 C3 EC D2 "
            		+ "20 52 A0 06 5A 73 99 A6 D1 95 94 E2 4B 15 39 30 "
            		+ "E6 3A 3B 12 B3 BA 4F 30 A3 CD A1 97 7D 60 D4 06 "
            		+ "0F FF 25 F8 4A 84 51 63 E1 12 82 76 5F B8 40 5F "
            		+ "1D BE 5E 50 E9 2A 6B 67 37 7E 07 9D 98 61 55 C0 "
            		+ "D8 2D 96 4E 65 33 2F 52 48 10 ED 78 31 A5 28 37 "
            		+ "F1 C0 FB 04 2E A2 A2 55 48 E3 B4 44 C3 37 F5 4C "
            		+ "75 47 B2 D8 77 73 4E 28 99 F4 0B FA 23 ED 51";

            byte[] payload = Hex.decode(peersPacketRaw);

            PeersMessage peersMessage = new PeersMessage(payload);
            peersMessage.getPeers();
        }
        long time2 = System.currentTimeMillis();

        System.out.println("20,000 PEERS packets parsing: " + (time2 - time1) + "(msec)");
    }
}
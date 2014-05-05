package org.ethereum.manager;

import com.maxmind.geoip.Location;

import org.ethereum.geodb.IpGeoDB;
import org.ethereum.net.vo.Block;
import org.ethereum.net.vo.PeerData;
import org.ethereum.net.vo.Transaction;

import java.util.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 20:35
 */
public class MainData {

    private Set<PeerData> peers = Collections.synchronizedSet(new HashSet<PeerData>());

    public static MainData instance = new MainData();

    public void addPeers(List<PeerData> newPeers){
        this.peers.addAll(newPeers);
        for (PeerData peerData : this.peers){
            Location location = IpGeoDB.getLocationForIp(peerData.getInetAddress());
            if (location != null)
                System.out.println("Hello: " + " [" + peerData.getInetAddress().toString()
                        + "] " + location.countryName);
        }
    }

    public void addBlocks(List<Block> blocks) {}
    public void addTransactions(List<Transaction> transactions) {}
}

package org.ethereum.manager;

import com.maxmind.geoip.Location;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import org.ethereum.geodb.IpGeoDB;
import org.ethereum.net.vo.PeerData;

import java.util.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 20:35
 */
public class MainData {

    private Set<PeerData> peers = Collections.synchronizedSet(new HashSet<PeerData>());

    private List blocks = Collections.synchronizedList(new ArrayList());
    private List transactions = Collections.synchronizedList(new ArrayList());

    public static MainData instance = new MainData();

    public void addPeers(List newPeers){
        this.peers.addAll(newPeers);


        for (PeerData peerData : this.peers){

            Location location = IpGeoDB.getLocationForIp(peerData.getInetAddress());
            if (location != null)
                System.out.println("Hello: " + " [" + peerData.getInetAddress().toString()
                        + "] " + location.countryName);

        }
    }

    public void addBlocks(List blocks){}
    public void addTransactions(List transactions){}

}

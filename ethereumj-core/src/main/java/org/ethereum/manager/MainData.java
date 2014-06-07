package org.ethereum.manager;

import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ethereum.core.AccountState;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.IpGeoDB;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip.Location;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 20:35
 */
public class MainData {

    Logger logger = LoggerFactory.getLogger(getClass().getName());

    private List<PeerData> peers = Collections.synchronizedList(new ArrayList<PeerData>());
    private Blockchain blockChain;
    private Wallet wallet = new Wallet();
    private ClientPeer activePeer;

    PeerDiscovery peerDiscovery;

    public static MainData instance = new MainData();

    public MainData() {
        // Initialize Wallet
        byte[] cowAddr = HashUtil.sha3("cow".getBytes());
        ECKey key = ECKey.fromPrivate(cowAddr);

        wallet.importKey(cowAddr);
        AccountState state = wallet.getAddressState(key.getAddress());
        state.addToBalance(BigInteger.valueOf(2).pow(200));
        wallet.importKey(HashUtil.sha3("cat".getBytes()));

        String secret = CONFIG.coinbaseSecret();
        byte[] cbAddr = HashUtil.sha3(secret.getBytes());
        wallet.importKey(cbAddr);


    	// Initialize Blockchain
    	blockChain = new Blockchain(wallet);

    	// Initialize PeerData
        try {
        	InetAddress ip = InetAddress.getByName(CONFIG.peerDiscoveryIP());
            int port = CONFIG.peerDiscoveryPort();
            PeerData peer = new PeerData(ip.getAddress(), port, new byte[]{00});
            peers.add(peer);
            peerDiscovery = new PeerDiscovery(peers);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Blockchain getBlockchain() {
    	return blockChain;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setActivePeer(ClientPeer peer){
        this.activePeer = peer;
    }

    public ClientPeer getActivePeer() {
        return activePeer;
    }

    public List<PeerData> getPeers() {
        return peers;
    }

    public void updatePeerIsDead(String ip, short port){
        for (PeerData peer : peers) {
            if (peer.getInetAddress().getHostAddress().equals(ip) && (peer.getPort() == port)){
                System.out.println("update peer is dead: " + ip + ":" + port);
                peer.setOnline(false);
                break;
            }
        }
    }

    public void addPeers(List<PeerData> newPeers){
        for (PeerData peer : newPeers){
            if (this.peers.indexOf(peer) == -1){
                Location location = IpGeoDB.getLocationForIp(peer.getInetAddress());
                if (location != null){
                    this.peers.add(peer);
                    if (peerDiscovery.isStarted())
                        peerDiscovery.addNewPeerData(peer);
                }
            }
        }
    }

    public void startPeerDiscovery(){
        peerDiscovery.start();
    };
}

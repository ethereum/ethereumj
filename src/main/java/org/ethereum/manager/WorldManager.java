package org.ethereum.manager;

import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.ethereum.core.AccountState;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.peerdiscovery.PeerDiscovery;

/**
 * WorldManager is the main class to handle the processing of transactions and
 * managing the world state.
 * 
 * www.ethereumJ.com
 * @author: Roman Mandeleil 
 * Created on: 01/06/2014 10:44
 */
public class WorldManager {

	private Blockchain blockchain;
	private final Repository repository;
	private Wallet wallet;

    private PeerDiscovery peerDiscovery;
    
    private final BlockingQueue<PeerData> peers = new LinkedBlockingQueue<>();
    
    private ClientPeer activePeer;

    private EthereumListener listener;
    
    private static final class WorldManagerHolder {
    	private static final WorldManager instance = new WorldManager();
    }
    
	private WorldManager() {
		this.repository = new Repository();
		this.blockchain = new Blockchain(repository);
		
        // Initialize PeerData
        final Set<PeerData> peerDataList = parsePeerDiscoveryIpList(CONFIG.peerDiscoveryIPList());
        peers.addAll(peerDataList);

        peerDiscovery = new PeerDiscovery(peers);

		this.wallet = new Wallet();
		
		byte[] cowAddr = HashUtil.sha3("cow".getBytes());
		ECKey key = ECKey.fromPrivate(cowAddr);
		wallet.importKey(cowAddr);

		AccountState state = wallet.getAccountState(key.getAddress());
		state.addToBalance(BigInteger.valueOf(2).pow(200));

		String secret = CONFIG.coinbaseSecret();
		byte[] cbAddr = HashUtil.sha3(secret.getBytes());
		wallet.importKey(cbAddr);
	}
	
	public static WorldManager getInstance() {
		return WorldManagerHolder.instance;
	}
    
    public void addListener(EthereumListener listener){
        this.listener = listener;
    }

    public void addPeers(final Set<PeerData> newPeers) {
        for (final PeerData peer : newPeers) {
        	peers.add(peer);
        	if (peerDiscovery.isStarted()) {
        		peerDiscovery.addNewPeerData(peer);
        	}
        }
    }

    public void startPeerDiscovery() {
        if (!peerDiscovery.isStarted())
            peerDiscovery.start();
    };

    public void stopPeerDiscover(){

        if (listener != null)
            listener.trace("Stopping peer discovery");

        if (peerDiscovery.isStarted())
            peerDiscovery.stop();
    }

    public void close() {
		repository.close();
	}

    public EthereumListener getListener() {
        return listener;
    }

    public Set<PeerData> parsePeerDiscoveryIpList(final String peerDiscoveryIpList){

        final List<String> ipList = Arrays.asList( peerDiscoveryIpList.split(",") );
        final Set<PeerData> peers = new HashSet<>();

        for (String ip : ipList){
            String[] addr = ip.trim().split(":");
            String ip_trim = addr[0];
            String port_trim = addr[1];

            try {
                InetAddress iAddr = InetAddress.getByName(ip_trim);
                int port = Integer.parseInt(port_trim);

                PeerData peerData = new PeerData(iAddr.getAddress(), port, new byte[]{00});
                peers.add(peerData);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return peers;
    }
    
    public void setWallet(Wallet wallet)  {
    	this.wallet = wallet;
    }

	public Repository getRepository() {
		return repository;
	}
	
	public Blockchain getBlockchain() {
		return blockchain;
	}
	
	public void loadBlockchain() {
		this.blockchain = repository.loadBlockchain();
	}

	public Wallet getWallet() {
		return wallet;
	}

    public void setActivePeer(ClientPeer peer) {
        this.activePeer = peer;
    }

    public ClientPeer getActivePeer() {
        return activePeer;
    }

    public BlockingQueue<PeerData> getPeers() {
		return peers;
	}

}

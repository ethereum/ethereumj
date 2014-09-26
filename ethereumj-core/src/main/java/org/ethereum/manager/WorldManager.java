package org.ethereum.manager;

import static org.ethereum.config.SystemProperties.CONFIG;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.Peer;
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
	private Repository repository;
	private Wallet wallet;

    private PeerDiscovery peerDiscovery;
    
    private final Set<Peer> peers = Collections.synchronizedSet(new HashSet<Peer>());
    
    private ClientPeer activePeer;

    private EthereumListener listener;
    
    private static final class WorldManagerHolder {
    	private static final WorldManager instance = new WorldManager();
        static{
            instance.init();
        }
    }
    
	private WorldManager() {
		this.repository = new RepositoryImpl();
		this.blockchain = new BlockchainImpl(repository);
		
        // Initialize PeerData
        List<Peer> peerDataList = parsePeerDiscoveryIpList(CONFIG.peerDiscoveryIPList());
        peers.addAll(peerDataList);

        peerDiscovery = new PeerDiscovery(peers);
	}

    // used for testing
    public void reset() {
        this.repository = new RepositoryImpl();
        this.blockchain = new BlockchainImpl(repository);
    }

    public void init() {

    	this.wallet = new Wallet();
        byte[] cowAddr = HashUtil.sha3("cow".getBytes());
        wallet.importKey(cowAddr);

//        AccountState state = wallet.getAccountState(key.getAddress());
//        state.addToBalance(BigInteger.valueOf(2).pow(200));

        String secret = CONFIG.coinbaseSecret();
        byte[] cbAddr = HashUtil.sha3(secret.getBytes());
        wallet.importKey(cbAddr);
    }
	
	public static WorldManager getInstance() {
		return WorldManagerHolder.instance;
	}
    
    public void addListener(EthereumListener listener) {
        this.listener = listener;
    }

    public void addPeers(final Set<Peer> newPeers) {

        synchronized (peers) {
            for (final Peer peer : newPeers) {
                if (peerDiscovery.isStarted() && !peers.contains(peer)) {
                    peerDiscovery.addNewPeerData(peer);
                }
                peers.add(peer);
            }
        }

    }

    public void startPeerDiscovery() {
        if (!peerDiscovery.isStarted())
            peerDiscovery.start();
    }

    public void stopPeerDiscovery(){

        if (listener != null)
            listener.trace("Stopping peer discovery");

        if (peerDiscovery.isStarted())
            peerDiscovery.stop();
    }

    public EthereumListener getListener() {
        return listener;
    }

    public List<Peer> parsePeerDiscoveryIpList(final String peerDiscoveryIpList) {

        final List<String> ipList = Arrays.asList(peerDiscoveryIpList.split(","));
        final List<Peer> peers = new ArrayList<>();

        for (String ip : ipList){
            String[] addr = ip.trim().split(":");
            String ip_trim = addr[0];
            String port_trim = addr[1];

            try {
                InetAddress iAddr = InetAddress.getByName(ip_trim);
                int port = Integer.parseInt(port_trim);

                Peer peerData = new Peer(iAddr.getAddress(), port, new byte[]{00});
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

    public Set<Peer> getPeers() {
		return peers;
	}


    public boolean isBlockChainLoading(){

        if (blockchain.getBlockQueue().size() > 2)
            return true;
        else
            return false;
    }

    public void close() {
        stopPeerDiscovery();
        repository.close();
        blockchain.close();
    }

}

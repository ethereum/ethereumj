package org.ethereum.manager;

import static org.ethereum.config.SystemProperties.CONFIG;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.ethereum.core.AccountState;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.submit.WalletTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * WorldManager is the main class to handle the processing of transactions and
 * managing the world state.
 * 
 * www.ethereumJ.com
 * @author: Roman Mandeleil 
 * Created on: 01/06/2014 10:44
 */
public class WorldManager {

	private Logger logger = LoggerFactory.getLogger("main");

	private Blockchain blockchain;
	private Repository repository;
	private Wallet wallet;

    private PeerDiscovery peerDiscovery;
    private List<PeerData> peers = Collections.synchronizedList(new ArrayList<PeerData>());
    private ClientPeer activePeer;

    // This map of transaction designed
    // to approve the tx by external trusted peer
    private Map<String, WalletTransaction> walletTransactions =
            Collections.synchronizedMap(new HashMap<String, WalletTransaction>());

    private EthereumListener listener;

	private static WorldManager instance;

	private WorldManager() {
		this.repository = new Repository();
		this.blockchain = new Blockchain(repository);
		
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
		if(instance == null) {
			instance = new WorldManager();
		}
		return instance;
	}
	
    /***********************************************************************
     *	1) the dialog put a pending transaction on the list
     *  2) the dialog send the transaction to a net
     *  3) wherever the transaction got in from the wire it will change to approve state
     *  4) only after the approve a) Wallet state changes
     *  5) After the block is received with that tx the pending been clean up
     */
    public WalletTransaction addWalletTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction placed hash: {}", hash );

        WalletTransaction walletTransaction =  this.walletTransactions.get(hash);
		if (walletTransaction != null)
			walletTransaction.incApproved();
		else {
			walletTransaction = new WalletTransaction(transaction);
			this.walletTransactions.put(hash, walletTransaction);
		}
        return walletTransaction;
    }

    public void removeWalletTransaction(Transaction transaction) {
        String hash = Hex.toHexString(transaction.getHash());
        logger.info("pending transaction removed with hash: {} ",  hash);
        walletTransactions.remove(hash);
    }
        
    public void setRepository(Repository repository)  {
    	this.repository = repository;
    }
    
    public void setBlockchain(Blockchain blockchain)  {
    	this.blockchain = blockchain;
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

    public List<PeerData> getPeers() {
        return peers;
    }

    public void addListener(EthereumListener listener){
        this.listener = listener;
    }

    public void addPeers(List<PeerData> newPeers) {
        for (PeerData peer : newPeers) {
            if (this.peers.indexOf(peer) == -1) {

                this.peers.add(peer);
                if (peerDiscovery.isStarted())
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
}

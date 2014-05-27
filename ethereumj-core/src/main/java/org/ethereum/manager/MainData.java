package org.ethereum.manager;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import com.maxmind.geoip.Location;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.geodb.IpGeoDB;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.submit.PendingTransaction;
import org.ethereum.wallet.AddressState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 20:35
 */
public class MainData {

    Logger logger = LoggerFactory.getLogger(getClass().getName());

    private List<PeerData> peers = Collections.synchronizedList(new ArrayList<PeerData>());
    private List<Block> blockChainDB = new ArrayList<Block>();
    private Wallet wallet = new Wallet();
    private ClientPeer activePeer;

    private long gasPrice = 1000;

    private Map<BigInteger, PendingTransaction> pendingTransactions =
            Collections.synchronizedMap(new HashMap<BigInteger, PendingTransaction>());

    PeerDiscovery peerDiscovery;

    public static MainData instance = new MainData();

    public MainData() {

        InetAddress ip = null;
        int port = 0;
        try {
            ip = InetAddress.getByName(CONFIG.peerDiscoveryIP());
            port = CONFIG.peerDiscoveryPort();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        PeerData peer = new PeerData(
                ip.getAddress(), port, new byte[]{00});
        peers.add(peer);

        byte[] cowAddr = HashUtil.sha3("cow".getBytes());
        ECKey key = ECKey.fromPrivate(cowAddr);

        wallet.importKey(cowAddr);
        AddressState state = wallet.getAddressState(key.getAddress());
        state.addToBalance(BigInteger.valueOf(2).pow(200)); // 1606938044258990275541962092341162602522202993782792835301376
        wallet.importKey(HashUtil.sha3("cat".getBytes()));

        peerDiscovery = new PeerDiscovery(peers);
    }

    public void addBlocks(List<Block> blocks) {

        // TODO: redesign this part when the state part and the genesis block is ready

        if (blocks.isEmpty()) return;

        Block firstBlockToAdd = blocks.get(blocks.size() - 1);

        // if it is the first block to add
        // check that the parent is the genesis
        if (blockChainDB.isEmpty() &&
            !Arrays.equals(StaticMessages.GENESIS_HASH, firstBlockToAdd.getParentHash())){
             return;
        }

        // if there is some blocks already
        // keep chain continuity
        if (!blockChainDB.isEmpty() ){
            Block lastBlock = blockChainDB.get(blockChainDB.size() - 1);
            String hashLast = Hex.toHexString(lastBlock.getHash());
            String blockParentHash = Hex.toHexString(firstBlockToAdd.getParentHash());
            if (!hashLast.equals(blockParentHash)) return;
        }

        for (int i = blocks.size() - 1; i >= 0 ; --i){
            Block block = blocks.get(i);
            blockChainDB.add(block);

            if (logger.isInfoEnabled())
                logger.info("block added to the chain hash: {}", Hex.toHexString(block.getHash()));

            this.gasPrice = block.getMinGasPrice();


            wallet.processBlock(block);
        }

        // Remove all pending transactions as they already approved by the net
        for (Block block : blocks){
            for (Transaction tx : block.getTransactionsList()){
                if (logger.isDebugEnabled())
                    logger.debug("pending cleanup: tx.hash: [{}]", Hex.toHexString( tx.getHash()));
                pendingTransactions.remove(tx.getHash());
            }
        }
        logger.info("*** Block chain size: [ {} ]", blockChainDB.size());
    }

    public byte[] getLatestBlockHash(){
        if (blockChainDB.isEmpty())
            return StaticMessages.GENESIS_HASH;
        else
          return blockChainDB.get(blockChainDB.size() - 1).getHash();
    }

    public List<Block> getAllBlocks(){
        return blockChainDB;
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

    /*
     *        1) the dialog put a pending transaction on the list
     *        2) the dialog send the transaction to a net
     *        3) wherever the transaction got for the wire in will change to approve state
     *        4) only after the approve a) Wallet state changes
     *
     *        5) After the block is received with that tx the pending been clean up
    */
    public PendingTransaction addPendingTransaction(Transaction transaction) {

        BigInteger hash = new BigInteger(transaction.getHash());

        PendingTransaction pendingTransaction =  pendingTransactions.get(hash);
		if (pendingTransaction != null)
			pendingTransaction.incApproved();
		else {
			pendingTransaction = new PendingTransaction(transaction);
			pendingTransactions.put(hash, pendingTransaction);
		}
        return pendingTransaction;
    }

    public long getGasPrice() {
        return gasPrice;
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

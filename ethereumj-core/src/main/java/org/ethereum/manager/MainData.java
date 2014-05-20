package org.ethereum.manager;

import com.maxmind.geoip.Location;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.HashUtil;
import org.ethereum.geodb.IpGeoDB;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.message.StaticMessages;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 20:35
 */
public class MainData {



    private Set<PeerData> peers = Collections.synchronizedSet(new HashSet<PeerData>());
    private List<Block> blockChainDB = new ArrayList<Block>();
    private Wallet wallet = new Wallet();
    private ClientPeer activePeer;

    public static MainData instance = new MainData();

    public MainData() {

        wallet.importKey(HashUtil.sha3("cow".getBytes()));
        wallet.importKey(HashUtil.sha3("cat".getBytes()));
    }

    public void addPeers(List<PeerData> newPeers){
        this.peers.addAll(newPeers);
        for (PeerData peerData : this.peers){
            Location location = IpGeoDB.getLocationForIp(peerData.getInetAddress());
            if (location != null)
                System.out.println("Hello: " + " [" + peerData.getInetAddress().toString()
                        + "] " + location.countryName);
        }
    }

    public void addBlocks(List<Block> blocks) {

        // TODO: redesign this part when the state part and the genesis block is ready

        if (blocks.isEmpty()) return;

        Block firstBlockToAdd = blocks.get(blocks.size() - 1);

        // if it is the first block to add
        // check that the parent is the genesis
        if (blockChainDB.isEmpty() &&
            !Arrays.equals(StaticMessages.GENESSIS_HASH, firstBlockToAdd.getParentHash())){

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

        for (int i = blocks.size() - 1; i > 0 ; --i){
            Block block = blocks.get(i);
            blockChainDB.add(block);
            wallet.processBlock(block);
        }

        System.out.println("*** Block chain size: [" + blockChainDB.size() + "]");
    }


    public byte[] getLatestBlockHash(){

        if (blockChainDB.isEmpty())
            return StaticMessages.GENESSIS_HASH;
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

    public void addTransactions(List<Transaction> transactions) {}
}

package org.ethereum.manager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.maxmind.geoip.Location;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.geodb.IpGeoDB;
import org.ethereum.net.client.ClientPeer;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.message.StaticMessages;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.peerdiscovery.WorkerThread;
import org.ethereum.wallet.AddressState;
import org.spongycastle.util.encoders.Hex;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 20:35
 */
public class MainData {

    private List<PeerData> peers = Collections.synchronizedList(new ArrayList<PeerData>());
    private List<Block> blockChainDB = new ArrayList<Block>();
    private Wallet wallet = new Wallet();
    private ClientPeer activePeer;

    PeerDiscovery peerDiscovery;

    public static MainData instance = new MainData();

    public MainData() {

        PeerData peer = new PeerData(
                new byte[]{54 , (byte)201, 28, 117}, (short) 30303, new byte[]{00});
        peers.add(peer);


        byte[] cowAddr = HashUtil.sha3("cow".getBytes());
        ECKey key = ECKey.fromPrivate(cowAddr);

        wallet.importKey(cowAddr);
        AddressState state = wallet.getAddressState(key.getAddress());
        state.addToBalance(new BigInteger("1606938044258990275541962092341162602522202993782792835301376"));
        wallet.importKey(HashUtil.sha3("cat".getBytes()));

        peerDiscovery = new PeerDiscovery(peers);
        peerDiscovery.start();
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
            wallet.processBlock(block);
        }

        System.out.println("*** Block chain size: [" + blockChainDB.size() + "]");
    }

    public byte[] getLatestBlockHash(){
        if (blockChainDB.isEmpty())
            return (new Genesis()).getHash();
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
                    peerDiscovery.addNewPeerData(peer);
                }
            }
        }

    }

}

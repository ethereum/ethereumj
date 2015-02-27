package org.ethereum.net.dht;

import java.util.ArrayList;
import java.util.List;

public class Bucket {

    public static int MAX_KADEMLIA_K = 5;

    // if bit = 1 go left
    Bucket left;

    // if bit = 0 go right
    Bucket right;

    String name;

    List<PeerId> peerIds = new ArrayList<>();


    public Bucket(String name) {
        this.name = name;
    }


    public void add(PeerId peerId) {

        if (peerId == null) throw new Error("Not a leaf");

        if ( peerIds == null){

            if (peerId.nextBit(name) == 1)
                left.add(peerId);
            else
                right.add(peerId);

            return;
        }

        peerIds.add(peerId);

        if (peerIds.size() > MAX_KADEMLIA_K)
            splitBucket();
    }

    public void splitBucket() {
        left = new Bucket(name + "1");
        right = new Bucket(name + "0");

        for (PeerId id : peerIds) {
            if (id.nextBit(name) == 1)
                left.add(id);
            else
                right.add(id);
        }

        this.peerIds = null;
    }


    public Bucket left() {
        return left;
    }

    public Bucket right() {
        return right;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(name).append("\n");

        if (peerIds == null) return sb.toString();

        for (PeerId id : peerIds)
            sb.append(id.toBinaryString()).append("\n");

        return sb.toString();
    }


    public void traverseTree(DoOnTree doOnTree) {

        if (left  != null) left.traverseTree(doOnTree);
        if (right != null) right.traverseTree(doOnTree);

        doOnTree.call(this);
    }


    /********************/
     // tree operations //
    /********************/

    public interface DoOnTree {

        public void call(Bucket bucket);
    }


    public static class SaveLeaf implements DoOnTree {

        List<Bucket> leafs = new ArrayList<>();

        @Override
        public void call(Bucket bucket) {
            if (bucket.peerIds != null) leafs.add(bucket);
        }

        public List<Bucket> getLeafs() {
            return leafs;
        }

        public void setLeafs(List<Bucket> leafs) {
            this.leafs = leafs;
        }
    }

    public String getName(){
        return name;
    }

    public List<PeerId> getPeerIds() {
        return peerIds;
    }
}

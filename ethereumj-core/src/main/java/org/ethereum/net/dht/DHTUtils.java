package org.ethereum.net.dht;

import java.util.List;

import static org.ethereum.net.dht.Bucket.*;

public class DHTUtils {

    public static void printAllLeafs(Bucket root){
        SaveLeaf saveLeaf = new SaveLeaf();
        root.traverseTree(saveLeaf);

        for (Bucket bucket : saveLeaf.getLeafs())
            System.out.println(bucket);
    }

    public static List<Bucket> getAllLeafs(Bucket root){
        SaveLeaf saveLeaf = new SaveLeaf();
        root.traverseTree(saveLeaf);

        return saveLeaf.getLeafs();
    }
}

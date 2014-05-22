package org.ethereum.net.peerdiscovery;

import org.ethereum.net.client.PeerData;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 22/05/2014 09:26
 */

public class WorkerThread implements Runnable {

    ThreadPoolExecutor poolExecutor;
    private PeerData peerData;

    public WorkerThread(PeerData peerData, ThreadPoolExecutor poolExecutor){
        this.poolExecutor = poolExecutor;
        this.peerData = peerData;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start. Command = "+ peerData.toString());
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End.");
        poolExecutor.execute(this);
    }

    private void processCommand() {

        try {
            PeerTaster peerTaster = new PeerTaster();
            peerTaster.connect(peerData.getInetAddress().getHostName(), peerData.getPort());
            peerData.setOnline(true);
            System.out.println("Peer: " + peerData.toString() + " isOnline: true");
        }
        catch (Throwable e) {
            System.out.println("Peer: " + peerData.toString() + " isOnline: false");
            peerData.setOnline(false);
        }
    }

    @Override
    public String toString(){
        return " Worker for: " + this.peerData.toString();
    }
}

package org.ethereum.net.peerdiscovery;


import org.ethereum.net.client.PeerData;

import java.util.List;
import java.util.concurrent.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 22/05/2014 09:10
 */

public class PeerDiscovery {

    RejectedExecutionHandlerImpl rejectionHandler;
    ThreadFactory threadFactory;
    ThreadPoolExecutor executorPool;
    PeerDiscoveryMonitorThread monitor;
    List<PeerData> peers;

    boolean started = false;

    public PeerDiscovery(List<PeerData> peers) {
        this.peers = peers;
    }

    public void start(){

        //RejectedExecutionHandler implementation
        rejectionHandler = new RejectedExecutionHandlerImpl();

        //Get the ThreadFactory implementation to use
        threadFactory = Executors.defaultThreadFactory();

        //creating the ThreadPoolExecutor
        executorPool = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler);

        //start the monitoring thread
        monitor = new PeerDiscoveryMonitorThread(executorPool, 3);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        for (PeerData peerData : this.peers) {
            executorPool.execute(new WorkerThread(peerData, executorPool));
        }

        started = true;
    }

    public void addNewPeerData(PeerData peerData){
        executorPool.execute(new WorkerThread(peerData, executorPool));
    }

    public void stop(){
        executorPool.shutdown();
        monitor.shutdown();
    }


    public boolean isStarted() {
        return started;
    }

    // todo: this main here for test erase it once upon a time
    public static void main(String args[]) throws InterruptedException{

        //RejectedExecutionHandler implementation
        RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();

        //Get the ThreadFactory implementation to use
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        //creating the ThreadPoolExecutor
        ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler);

        //start the monitoring thread
        PeerDiscoveryMonitorThread monitor = new PeerDiscoveryMonitorThread(executorPool, 3);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        //submit work to the thread pool
        PeerData peer = new PeerData(new byte[]{54, (byte)211, 14, 10}, (short) 30303, new byte[]{00});
        executorPool.execute(new WorkerThread(peer, executorPool));

        PeerData peer2 = new PeerData(new byte[]{54 , (byte)201, 28, 117}, (short) 30303, new byte[]{00});
        executorPool.execute(new WorkerThread(peer2, executorPool));

        PeerData peer3 = new PeerData(new byte[]{54, (byte)211, 14, 10}, (short) 40404, new byte[]{00});
        executorPool.execute(new WorkerThread(peer3, executorPool));

        Thread.sleep(30000);
        //shut down the pool
        executorPool.shutdown();
        //shut down the monitor thread
        Thread.sleep(5000);
        monitor.shutdown();

    }


}


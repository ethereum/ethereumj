package org.ethereum.net.peerdiscovery;

import org.ethereum.net.client.PeerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 22/05/2014 09:10
 */
public class PeerDiscovery {
	
	private static final Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private RejectedExecutionHandlerImpl rejectionHandler;
    private ThreadFactory threadFactory;
    private ThreadPoolExecutor executorPool;
    private PeerDiscoveryMonitorThread monitor;
    
    private final Queue<PeerData> peers;

    private final AtomicBoolean started = new AtomicBoolean(false);

    public PeerDiscovery(Queue<PeerData> peers) {
        this.peers = peers;
    }

    public void start() {

        //RejectedExecutionHandler implementation
        rejectionHandler = new RejectedExecutionHandlerImpl();

        //Get the ThreadFactory implementation to use
        threadFactory = Executors.defaultThreadFactory();

        //creating the ThreadPoolExecutor
        executorPool = new ThreadPoolExecutor(CONFIG.peerDiscoveryWorkers(), 1000, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(CONFIG.peerDiscoveryWorkers()), threadFactory, rejectionHandler);

        //start the monitoring thread
        monitor = new PeerDiscoveryMonitorThread(executorPool, 0);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        for (PeerData peerData : this.peers) {
            executorPool.execute(new WorkerThread(peerData, executorPool));
        }
        
        started.set(true);
    }

    public void addNewPeerData(PeerData peerData) {
        logger.debug("add new peer for discovery: {}", peerData);
        executorPool.execute(new WorkerThread(peerData, executorPool));
    }

    public void stop() {
        executorPool.shutdown();
        monitor.shutdown();
    }

    public boolean isStarted() {
        return started.get();
    }

}


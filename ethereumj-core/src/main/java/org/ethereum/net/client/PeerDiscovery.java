package org.ethereum.net.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * Created on: 22/05/2014 09:10
 */
public class PeerDiscovery {

	private static final Logger logger = LoggerFactory.getLogger("peerdiscovery");

	private final Set<Peer> peers = Collections.synchronizedSet(new HashSet<Peer>());
	
	private PeerMonitorThread monitor;
	private ThreadFactory threadFactory;
	private ThreadPoolExecutor executorPool;
	private RejectedExecutionHandler rejectionHandler;

	private final AtomicBoolean started = new AtomicBoolean(false);

	public void start() {

		// RejectedExecutionHandler implementation
		rejectionHandler = new RejectionLogger();

		// Get the ThreadFactory implementation to use
		threadFactory = Executors.defaultThreadFactory();

		// creating the ThreadPoolExecutor
		executorPool = new ThreadPoolExecutor(CONFIG.peerDiscoveryWorkers(),
				1000, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(
						CONFIG.peerDiscoveryWorkers()), threadFactory, rejectionHandler);

		// start the monitoring thread
		monitor = new PeerMonitorThread(executorPool, 1);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();

		started.set(true);
	}

	public void stop() {
		executorPool.shutdown();
		monitor.shutdown();
		started.set(false);
	}

	public boolean isStarted() {
		return started.get();
	}
	
    /**
     * Update list of known peers with new peers
     * This method checks for duplicate peer id's and addresses
     *
     * @param newPeers to be added to the list of known peers
     */
    public void addPeers(final Set<Peer> newPeers) {
        synchronized (peers) {
            for (final Peer newPeer : newPeers) {
            	if(!peers.contains(newPeer))
                    addNewPeer(newPeer);
                peers.add(newPeer);
            }
        }
    }

	public void addNewPeer(Peer peer) {
		logger.debug("Add new peer for discovery: {}", peer);
		executorPool.execute(new WorkerThread(peer, executorPool));
	}

    public Set<Peer> getPeers() {
		return peers;
	}
	
}
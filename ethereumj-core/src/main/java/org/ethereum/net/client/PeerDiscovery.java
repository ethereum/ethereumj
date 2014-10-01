package org.ethereum.net.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * Created on: 22/05/2014 09:10
 */
public class PeerDiscovery {

	private static final Logger logger = LoggerFactory
			.getLogger("peerdiscovery");

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

	public void addNewPeer(Peer peer) {
		logger.debug("Add new peer for discovery: {}", peer);
		executorPool.execute(new WorkerThread(peer, executorPool));
	}

	public void stop() {
		executorPool.shutdown();
		monitor.shutdown();
		started.set(false);
	}

	public boolean isStarted() {
		return started.get();
	}
}
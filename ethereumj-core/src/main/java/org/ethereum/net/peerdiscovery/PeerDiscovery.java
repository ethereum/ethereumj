package org.ethereum.net.peerdiscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * Created on: 22/05/2014 09:10
 */
public class PeerDiscovery {

	private static final Logger logger = LoggerFactory.getLogger("peerdiscovery");

	private final Set<PeerData> peers = Collections.synchronizedSet(new HashSet<PeerData>());
	
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

        // Initialize PeerData
        List<PeerData> peerDataList = parsePeerDiscoveryIpList(CONFIG.peerDiscoveryIPList());
        peers.addAll(peerDataList);

        for (PeerData peerData : this.peers) {
            executorPool.execute(new WorkerThread(peerData, executorPool));
        }

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
	
    public Set<PeerData> getPeers() {
		return peers;
	}
		
    /**
     * Update list of known peers with new peers
     * This method checks for duplicate peer id's and addresses
     *
     * @param newPeers to be added to the list of known peers
     */
    public void addPeers(final Set<PeerData> newPeers) {
        synchronized (peers) {
			for (final PeerData newPeer : newPeers) {
                if (started.get() && !peers.contains(newPeer)){
                    startWorker(newPeer);
                }
                peers.add(newPeer);
            }
        }
    }

	private void startWorker(PeerData peer) {
		logger.debug("Add new peer for discovery: {}", peer);
		executorPool.execute(new WorkerThread(peer, executorPool));
	}

    public List<PeerData> parsePeerDiscoveryIpList(final String peerDiscoveryIpList){

        final List<String> ipList = Arrays.asList( peerDiscoveryIpList.split(",") );
        final List<PeerData> peers = new ArrayList<>();

        for (String ip : ipList){
            String[] addr = ip.trim().split(":");
            String ip_trim = addr[0];
            String port_trim = addr[1];

            try {
                InetAddress iAddr = InetAddress.getByName(ip_trim);
                int port = Integer.parseInt(port_trim);

                PeerData peerData = new PeerData(iAddr, port, "");
                peers.add(peerData);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return peers;
    }


}
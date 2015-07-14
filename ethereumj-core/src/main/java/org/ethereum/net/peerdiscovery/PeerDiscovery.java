package org.ethereum.net.peerdiscovery;

import org.ethereum.net.p2p.Peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
@Component
public class PeerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private final Set<PeerInfo> peers = Collections.synchronizedSet(new HashSet<PeerInfo>());

    private PeerMonitorThread monitor;
    private ThreadFactory threadFactory;
    private ThreadPoolExecutor executorPool;
    private RejectedExecutionHandler rejectionHandler;

    @Autowired
    private ApplicationContext ctx;


    private final AtomicBoolean started = new AtomicBoolean(false);

    public void start() {

        // RejectedExecutionHandler implementation
        rejectionHandler = new RejectionLogger();

        // Get the ThreadFactory implementation to use
        threadFactory = Executors.defaultThreadFactory();

        // creating the ThreadPoolExecutor
        executorPool = new ThreadPoolExecutor(CONFIG.peerDiscoveryWorkers(), CONFIG.peerDiscoveryWorkers(), 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000), threadFactory, rejectionHandler);

        // start the monitoring thread
        monitor = new PeerMonitorThread(executorPool, 1, this);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        // Initialize PeerData
        List<PeerInfo> peerDataList = parsePeerDiscoveryIpList(CONFIG.peerDiscoveryIPList());
        addPeers(peerDataList);

        for (PeerInfo peerData : this.peers) {
            WorkerThread workerThread = ctx.getBean(WorkerThread.class);
            workerThread.init(peerData, executorPool);
            executorPool.execute(workerThread);
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

    public Set<PeerInfo> getPeers() {
        return peers;
    }

    /**
     * Update list of known peers with new peers
     * This method checks for duplicate peer id's and addresses
     *
     * @param newPeers to be added to the list of known peers
     */
    public void addPeers(Set<Peer> newPeers) {
        synchronized (peers) {
            for (final Peer newPeer : newPeers) {
                PeerInfo peerInfo =
                        new PeerInfo(newPeer.getAddress(), newPeer.getPort(), newPeer.getPeerId());
                if (started.get() && !peers.contains(peerInfo)) {
                    startWorker(peerInfo);
                }
                peers.add(peerInfo);
            }
        }
    }

    public void addPeers(Collection<PeerInfo> newPeers) {
        synchronized (peers) {
            peers.addAll(newPeers);
        }
    }

    private void startWorker(PeerInfo peerInfo) {

        logger.debug("Add new peer for discovery: {}", peerInfo);
        WorkerThread workerThread = ctx.getBean(WorkerThread.class);
        workerThread.init(peerInfo, executorPool);
        executorPool.execute(workerThread);
    }

    public List<PeerInfo> parsePeerDiscoveryIpList(final List<String> ipList) {

        final List<PeerInfo> peers = new ArrayList<>();

        for (String ip : ipList) {
            String[] addr = ip.trim().split(":");
            String ip_trim = addr[0];
            String port_trim = addr[1];

            try {
                InetAddress iAddr = InetAddress.getByName(ip_trim);
                int port = Integer.parseInt(port_trim);

                PeerInfo peerData = new PeerInfo(iAddr, port, "");
                peers.add(peerData);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return peers;
    }


}
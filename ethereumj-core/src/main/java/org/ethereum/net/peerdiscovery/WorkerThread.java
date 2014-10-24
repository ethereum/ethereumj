package org.ethereum.net.peerdiscovery;

import org.ethereum.net.client.PeerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Roman Mandeleil 
 * Created on: 22/05/2014 09:26
 */
public class WorkerThread implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

	private PeerInfo peerInfo;
	private PeerClient clientPeer;
	private ThreadPoolExecutor poolExecutor;

	public WorkerThread(PeerInfo peer, ThreadPoolExecutor poolExecutor) {
		this.peerInfo = peer;
		this.poolExecutor = poolExecutor;
	}

	@Override
	public void run() {
		logger.debug("{} start", Thread.currentThread().getName());
		processCommand();
		logger.debug("{} end", Thread.currentThread().getName());

		poolExecutor.execute(this);
	}

	private void processCommand() {

		try {
			clientPeer = new PeerClient(true);
			clientPeer.connect(peerInfo.getAddress().getHostAddress(), peerInfo.getPort());
            peerInfo.setOnline(true);

            peerInfo.setHandshakeHelloMessage(clientPeer.getHelloHandshake());
            peerInfo.setStatusMessage( clientPeer.getStatusHandshake() );

            logger.info("Peer is online: [{}] ", peerInfo);

		} catch (Throwable e) {
			if (peerInfo.isOnline())
				logger.info("Peer: [{}] went offline, due to: [{}]", peerInfo
						.getAddress().getHostAddress(), e);
			peerInfo.setOnline(false);
		} finally {
			logger.info("Peer: " + peerInfo.toString() + " is "
					+ (peerInfo.isOnline() ? "online" : "offline"));
			peerInfo.setLastCheckTime(System.currentTimeMillis());

		}
	}

	@Override
	public String toString() {
		return "Worker for: " + this.peerInfo.toString();
	}
}

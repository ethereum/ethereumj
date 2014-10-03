package org.ethereum.net.client;

import java.util.concurrent.ThreadPoolExecutor;

import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerMonitorThread implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger("peermonitor");

	private ThreadPoolExecutor executor;
	private int seconds;
	private volatile boolean run = true;
	private StringBuffer toStringBuff = new StringBuffer();

	public PeerMonitorThread(ThreadPoolExecutor executor, int delay) {
		this.executor = executor;
		this.seconds = delay;
	}

	public void shutdown() {
		this.run = false;
	}

	@Override
	public void run() {
		while (run) {
			if (logger.isInfoEnabled()) {
				toStringBuff.setLength(0);
				toStringBuff.append("[monitor] [");
				toStringBuff.append(this.executor.getPoolSize());
				toStringBuff.append("/");
				toStringBuff.append(this.executor.getCorePoolSize());
				toStringBuff.append("] Active: ");
				toStringBuff.append(this.executor.getActiveCount());
				toStringBuff.append(", Completed: ");
				toStringBuff.append(this.executor.getCompletedTaskCount());
				toStringBuff.append(", Task: ");
				toStringBuff.append(this.executor.getTaskCount());
				toStringBuff.append(", isShutdown: ");
				toStringBuff.append(this.executor.isShutdown());
				toStringBuff.append(", isTerminated: ");
				toStringBuff.append(this.executor.isTerminated());
				toStringBuff.append(", peersDiscovered: ");
				toStringBuff.append(WorldManager.getInstance()
						.getPeerDiscovery().getPeers().size());
				logger.info(toStringBuff.toString());
			}
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException e) {
				logger.error("Thread interrupted", e);
			}
		}
	}
}
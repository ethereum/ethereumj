package org.ethereum.net.peerdiscovery;

import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class PeerDiscoveryMonitorThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private ThreadPoolExecutor executor;
    private int seconds;
    private volatile boolean run = true;

	public PeerDiscoveryMonitorThread(ThreadPoolExecutor executor, int delay) {
		this.executor = executor;
		this.seconds = delay;
	}

    public void shutdown() {
		this.run = false;
    }

    @Override
    public void run() {
        while(run) {
            logger.trace(
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s, peersDiscovered: %d ",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated(),
                            WorldManager.getInstance().getPeers().size()));
            try {
                Thread.sleep(seconds*1000);
            } catch (InterruptedException e) {
            	logger.error("Thread interrupted", e);
            }
        }
    }
}
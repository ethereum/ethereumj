package org.ethereum.net.peerdiscovery;

import org.ethereum.net.client.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 22/05/2014 09:26
 */
public class WorkerThread implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

    ThreadPoolExecutor poolExecutor;
    private Peer peerData;
    private PeerTaster peerTaster;

    public WorkerThread(Peer peerData, ThreadPoolExecutor poolExecutor) {
        this.poolExecutor = poolExecutor;
        this.peerData = peerData;
    }

    @Override
    public void run() {
        logger.info("{} start", Thread.currentThread().getName());
        processCommand();
        logger.info("{} end", Thread.currentThread().getName());

        poolExecutor.execute(this);
    }

    private void processCommand() {

        try {
            peerTaster = new PeerTaster();
            peerTaster.connect(peerData.getInetAddress().getHostAddress(), peerData.getPort());

            peerData.setOnline(true);
            peerData.setHandshake(peerTaster.getHandshake());
        }
        catch (Throwable e) {
            if (peerData.isOnline() == true)
                logger.info("Peer: [{}] went offline, due to: [{}]",
                        peerData.getInetAddress().getHostAddress(), e);
            peerData.setOnline(false);
        } finally {
            logger.info("Peer: " + peerData.toString() + " isOnline: " + peerData.isOnline());
            peerData.setLastCheckTime(System.currentTimeMillis());
        }
    }

    @Override
    public String toString() {
        return " Worker for: " + this.peerData.toString();
    }
}

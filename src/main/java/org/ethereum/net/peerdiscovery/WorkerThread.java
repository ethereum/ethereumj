package org.ethereum.net.peerdiscovery;

import org.ethereum.net.client.PeerData;
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

    private PeerData peerData;
    private PeerTaster peerTaster;

    public WorkerThread(PeerData peerData) {
        this.peerData = peerData;
    }

    @Override
    public void run() {
        logger.info(Thread.currentThread().getName()+" Start. Command = "+ peerData.toString());
        processCommand();
        logger.info(Thread.currentThread().getName()+" End.");
    }

    private void processCommand() {

        try {

            peerTaster = new PeerTaster();
            peerTaster.connect(peerData.getInetAddress().getHostAddress(), peerData.getPort());

            peerData.setOnline(true);
            peerData.setHandshake(peerTaster.getHandshake());
            logger.info("Peer: " + peerData.toString() + " isOnline: true");
        }
        catch (Throwable e) {
            if (peerData.isOnline() == true)
                logger.info("Peer: [ {} ] got offline, due: [ {} ]",
                        peerData.getInetAddress().getHostAddress(),
                        e);
            logger.info("Peer: " + peerData.toString() + " isOnline: false");
            peerData.setOnline(false);
        } finally {
            peerData.setLastCheckTime(System.currentTimeMillis());
        }
    }

    @Override
    public String toString() {
        return " Worker for: " + this.peerData.toString();
    }
}

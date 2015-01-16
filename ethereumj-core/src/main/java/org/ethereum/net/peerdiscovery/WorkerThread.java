package org.ethereum.net.peerdiscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
@Component
@Scope("prototype")
public class WorkerThread implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private PeerInfo peerInfo;
    private ThreadPoolExecutor poolExecutor;

    @Autowired
    ApplicationContext ctx;

    public WorkerThread() {
    }

    public void init(PeerInfo peer, ThreadPoolExecutor poolExecutor) {
        this.peerInfo = peer;
        this.poolExecutor = poolExecutor;
    }

    @Override
    public void run() {
        logger.debug("{} start", Thread.currentThread().getName());
        processCommand();
        logger.debug("{} end", Thread.currentThread().getName());

        sleep(1000);
        poolExecutor.execute(this);
    }

    private void processCommand() {

        try {

            DiscoveryChannel discoveryChannel = ctx.getBean(DiscoveryChannel.class);
            discoveryChannel.connect(peerInfo.getAddress().getHostAddress(), peerInfo.getPort());
            peerInfo.setOnline(true);

            peerInfo.setHandshakeHelloMessage(discoveryChannel.getHelloHandshake());
            peerInfo.setStatusMessage(discoveryChannel.getStatusHandshake());

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

    private void sleep(long milliseconds) {

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "Worker for: " + this.peerInfo.toString();
    }
}

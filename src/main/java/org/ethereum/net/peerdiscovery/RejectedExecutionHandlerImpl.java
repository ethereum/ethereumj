package org.ethereum.net.peerdiscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 22/05/2014 10:31
 */

public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    Logger logger = LoggerFactory.getLogger("peerdiscovery");

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.warn(r.toString() + " is rejected");
    }
}

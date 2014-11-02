package org.ethereum.net.server;

import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.shh.ShhHandler;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 01/11/2014 17:01
 */

public class Channel {

    MessageQueue msgQueue;
    P2pHandler p2pHandler;
    EthHandler ethHandler;
    ShhHandler shhHandler;


    public Channel(MessageQueue msgQueue, P2pHandler p2pHandler, EthHandler ethHandler, ShhHandler shhHandler) {
        this.msgQueue = msgQueue;
        this.p2pHandler = p2pHandler;
        this.ethHandler = ethHandler;
        this.shhHandler = shhHandler;
    }

    
}

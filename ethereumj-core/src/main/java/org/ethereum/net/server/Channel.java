package org.ethereum.net.server;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.wire.MessageDecoder;
import org.ethereum.net.wire.MessageEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 01/11/2014 17:01
 */
@Component
@Scope("prototype")
public class Channel {

    @Autowired
    ChannelManager channelManager;

    @Autowired
    MessageQueue msgQueue;

    @Autowired
    P2pHandler p2pHandler;

    @Autowired
    EthHandler ethHandler;

    @Autowired
    ShhHandler shhHandler;

    @Autowired
    MessageDecoder messageDecoder;

    @Autowired
    MessageEncoder messageEncoder;




    public Channel() {
    }

    public void init(){
        p2pHandler.setMsgQueue(msgQueue);
        ethHandler.setMsgQueue(msgQueue);
        shhHandler.setMsgQueue(msgQueue);
    }

    public P2pHandler getP2pHandler() {
        return p2pHandler;
    }

    public EthHandler getEthHandler() {
        return ethHandler;
    }

    public ShhHandler getShhHandler() {
        return shhHandler;
    }

    public MessageDecoder getMessageDecoder() {
        return messageDecoder;
    }

    public MessageEncoder getMessageEncoder() {
        return messageEncoder;
    }

    public void sendTransaction(Transaction tx){
        ethHandler.sendTransaction(tx);
    }

    public void sendNewBlock(Block block){

        // 1. check by best block send or not to send
        ethHandler.sendNewBlock(block);

    }

    public boolean isSync(){
        return ethHandler.getSyncStatus() == EthHandler.SyncSatus.SYNC_DONE;
    }


    public BigInteger getTotalDifficulty(){
        return ethHandler.getTotalDifficulty();
    }

    public void ethSync(){
        ethHandler.doSync();
    }

}

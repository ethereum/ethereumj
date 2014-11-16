package org.ethereum.net.server;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.db.ByteArrayWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/11/2014 13:38
 */
@Component
public class ChannelManager {

    private static final Logger logger = LoggerFactory.getLogger("net");

    Timer inactivesCollector = new Timer("inactivesCollector");
    List<Channel> channels = Collections.synchronizedList(new ArrayList<Channel>());

    Map<ByteArrayWrapper, Block> blockCache = new HashMap<>();

    public ChannelManager() {
    }

    public void recvTransaction(){
        // ???
    }


    public void recvBlock(){ // todo:
        // 1. Check in the cache if the hash exist
        // 2. Exist: go and send it to the queue
    }

    public void sendTransaction(Transaction tx){
        for (Channel channel : channels){
            channel.sendTransaction(tx);
        }
    }


    public void sendBlock(){
        // 1. Go over all channels and send the block
    }

    public void addChannel(Channel channel){
        synchronized (channels){
            channels.add(channel);
        }
    }

    public boolean isAllSync(){

        boolean result = true;
        for (Channel channel : channels){
            result &= channel.isSync();
        }

        return result;
    }

    public void scheduleChannelCollector(){
        inactivesCollector.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Iterator<Channel> iter = channels.iterator();
                while(iter.hasNext()){
                    Channel channel = iter.next();
                    if(!channel.p2pHandler.isActive()){
                        iter.remove();
                        logger.info("Channel removed: {}", channel.p2pHandler.getHandshakeHelloMessage());
                    }
                }
            }
        }, 2000, 5000);
    }
}

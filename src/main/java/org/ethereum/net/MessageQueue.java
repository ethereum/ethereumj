package org.ethereum.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.ethereum.net.message.*;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 09/07/2014 10:29
 */

public class MessageQueue {

    // 1) queue for messages
    // 2) process for sending messages

    // 3) messages open by send and answered by receive of appropriate message
    //                    GET_CHAIN by BLOCKS
    //                    PING  by PONG
    //                    GET_PEERS by PEERS
    //                    GET_TRANSACTIONS by TRANSACTIONS
    //                messages will not be answered: TRANSACTIONS, PONG, PEERS, TRANSACTIONS

    private Logger logger = LoggerFactory.getLogger("wire");

    Queue<MessageRoundtrip> messageQueue = new ConcurrentLinkedQueue<>();
    ChannelHandlerContext ctx = null;
    Timer timer = new Timer();

    public MessageQueue(ChannelHandlerContext ctx) {
        this.ctx = ctx;

        timer.scheduleAtFixedRate (new TimerTask() {

            public void run() {
                nudgeQueue();
            }
        }, 10, 10);
    }

    public void sendMessage(Message msg){

        if (msg instanceof  GetChainMessage && containsGetChain())
            return;

        messageQueue.add(new MessageRoundtrip(msg));
    }

    public void receivedMessage(Message msg){

        if (logger.isDebugEnabled())
            logger.debug("Recv: [ {} ] - [ {} ]",
                    msg.getMessageName(),
                    Hex.toHexString(msg.getPayload()));


            if (null != messageQueue.peek()) {

                MessageRoundtrip messageRoundtrip = messageQueue.peek();
                Message waitingMessage = messageRoundtrip.getMsg();

                if (waitingMessage.getAnswerMessage() == null) return;

                if (msg.getClass() == waitingMessage.getAnswerMessage()){
                    messageRoundtrip.answer();
                    logger.debug("Message round trip covered: [ {} ] ", messageRoundtrip.getMsg().getMessageName());
                }
        }
    }

    private void nudgeQueue(){

        // The message was answered, remove from the queue
        if (null != messageQueue.peek()) {

            MessageRoundtrip messageRoundtrip = messageQueue.peek();
            if (messageRoundtrip.isAnswered()) {
                messageQueue.remove();
            }
        }

        // Now send the next message
        if (null != messageQueue.peek()){

            MessageRoundtrip messageRoundtrip = messageQueue.peek();
            if (messageRoundtrip.getRetryTimes() == 0 ) {// todo: retry logic || messageRoundtrip.hasToRetry()){

                Message msg = messageRoundtrip.getMsg();
                sendToWire(msg);

                if (msg.getAnswerMessage() == null){

                    messageQueue.remove();
                }
                else {
                    messageRoundtrip.incRetryTimes();
                    messageRoundtrip.saveTime();
                }
            }
        }
    }

    private void sendToWire(Message msg){

        if (logger.isDebugEnabled())
            logger.debug("Send: [ {} ] - [ {} ]",
                    msg.getMessageName(),
                    Hex.toHexString(msg.getPayload()));

        ByteBuf buffer = ctx.alloc().buffer(msg.getPayload().length + 8);
        buffer.writeBytes(StaticMessages.MAGIC_PACKET);
        buffer.writeBytes(ByteUtil.calcPacketLength(msg.getPayload()));
        buffer.writeBytes(msg.getPayload());
        ctx.writeAndFlush(buffer);
    }


    private boolean containsGetChain(){
        Iterator<MessageRoundtrip> iterator = messageQueue.iterator();
        while(iterator.hasNext()){

            MessageRoundtrip msgRoundTrip = iterator.next();
            if (msgRoundTrip.getMsg() instanceof GetChainMessage)
                return true;
        }
        return false;
    }
}

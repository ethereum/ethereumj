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
 *	This class contains the logic for sending messages in a queue
 *
 *	Messages open by send and answered by receive of appropriate message
 *		GET_BLOCK_HASHES by BLOCK_HASHES
 *		GET_BLOCKS by BLOCKS
 *		PING by PONG
 *		GET_PEERS by PEERS
 *		GET_TRANSACTIONS by TRANSACTIONS
 *
 *	The following messages will not be answered: 
 *		PONG, PEERS, BLOCKS, TRANSACTIONS
 *
 * @author Roman Mandeleil
 */
public class MessageQueue {

    private Logger logger = LoggerFactory.getLogger("wire");

	private Queue<MessageRoundtrip> messageQueue = new ConcurrentLinkedQueue<>();
	private PeerListener listener;
	private ChannelHandlerContext ctx = null;
	private final Timer timer = new Timer();

	public MessageQueue(ChannelHandlerContext ctx, PeerListener listener) {
		this.ctx = ctx;
		this.listener = listener;

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				nudgeQueue();
			}
		}, 10, 10);
	}

    public void sendMessage(Message msg) {
		messageQueue.add(new MessageRoundtrip(msg));
    }

    public void receivedMessage(Message msg) throws InterruptedException {

    	if (listener != null)
    		listener.console("[Recv: " + msg + "]");
		if (logger.isInfoEnabled())
			logger.info("From: \t{} \tRecv: \t{}", ctx.channel().remoteAddress(), msg);
        if (logger.isDebugEnabled())
            logger.debug("Encoded: [{}]", Hex.toHexString(msg.getEncoded()));
        
        if (messageQueue.peek() != null) {
            MessageRoundtrip messageRoundtrip = messageQueue.peek();
            Message waitingMessage = messageRoundtrip.getMsg();

			if (waitingMessage.getAnswerMessage() != null
					&& msg.getClass() == waitingMessage.getAnswerMessage()) {
                messageRoundtrip.answer();
                logger.debug("Message round trip covered: [{}] ", messageRoundtrip.getMsg().getCommand());
            }
			if (msg instanceof DisconnectMessage) {
				ctx.close().sync();
				ctx.disconnect().sync();
			}
        }
    }

    private void nudgeQueue() {

        // The message was answered, remove from the queue
        if (messageQueue.peek() != null) {
            MessageRoundtrip messageRoundtrip = messageQueue.peek();
            if (messageRoundtrip.isAnswered()) {
                messageQueue.remove();
            }
        }

        // Now send the next message
        if (null != messageQueue.peek()) {

            MessageRoundtrip messageRoundtrip = messageQueue.peek();
            if (messageRoundtrip.getRetryTimes() == 0 ) {
            	// todo: retry logic || messageRoundtrip.hasToRetry()){

                Message msg = messageRoundtrip.getMsg();
                sendToWire(msg);

                if (msg.getAnswerMessage() == null)
                    messageQueue.remove();
                else {
                    messageRoundtrip.incRetryTimes();
                    messageRoundtrip.saveTime();
                }
            }
        }
    }

    private void sendToWire(Message msg) {

    	if (listener != null)
    		listener.console("[Send: " + msg + "]");
		if (logger.isInfoEnabled())
			logger.info("To: \t{} \tSend: \t{}", ctx.channel().remoteAddress(), msg);
        if (logger.isDebugEnabled())
            logger.debug("Encdoded: [{}]", Hex.toHexString(msg.getEncoded()));
        
        int packetLength = StaticMessages.SYNC_TOKEN.length + msg.getEncoded().length;
        ByteBuf buffer = ctx.alloc().buffer(packetLength);
        buffer.writeBytes(StaticMessages.SYNC_TOKEN);
        buffer.writeBytes(ByteUtil.calcPacketLength(msg.getEncoded()));
        buffer.writeBytes(msg.getEncoded());
        ctx.writeAndFlush(buffer);
    }
}

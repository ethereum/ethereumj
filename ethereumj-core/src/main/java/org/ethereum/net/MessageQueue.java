package org.ethereum.net;

import io.netty.channel.ChannelHandlerContext;

import org.ethereum.net.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *	This class contains the logic for sending messages in a queue
 *
 *	Messages open by send and answered by receive of appropriate message
 *		PING by PONG
 *		GET_PEERS by PEERS
 *		GET_TRANSACTIONS by TRANSACTIONS
 *		GET_BLOCK_HASHES by BLOCK_HASHES
 *		GET_BLOCKS by BLOCKS
 *
 *	The following messages will not be answered:
 *		PONG, PEERS, HELLO, STATUS, TRANSACTIONS, BLOCKS
 *
 * @author Roman Mandeleil
 */
public class MessageQueue {

	private Logger logger = LoggerFactory.getLogger("net");

	private Queue<MessageRoundtrip> messageQueue = new ConcurrentLinkedQueue<>();
	private PeerListener listener;
	private ChannelHandlerContext ctx = null;
	private final Timer timer = new Timer("MessageQueue");

	public MessageQueue(PeerListener listener) {
		this.listener = listener;
	}

    public void activate(ChannelHandlerContext ctx){
        this.ctx = ctx;
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

		if (messageQueue.peek() != null) {
			MessageRoundtrip messageRoundtrip = messageQueue.peek();
			Message waitingMessage = messageRoundtrip.getMsg();

			if (waitingMessage.getAnswerMessage() != null
					&& msg.getClass() == waitingMessage.getAnswerMessage()) {
				messageRoundtrip.answer();
				logger.debug("Message round trip covered: [{}] ",
						messageRoundtrip.getMsg().getClass());
			}
		}
	}

	private void removeAnsweredMessage(MessageRoundtrip messageRoundtrip) {
		if (messageRoundtrip != null && messageRoundtrip.isAnswered())
			messageQueue.remove();
	}

	private void nudgeQueue() {
		// remove last answered message on the queue
		removeAnsweredMessage(messageQueue.peek());
		// Now send the next message
		sendToWire(messageQueue.peek());
	}

	private void sendToWire(MessageRoundtrip messageRoundtrip) {

		if (messageRoundtrip != null && messageRoundtrip.getRetryTimes() == 0) {
			// TODO: retry logic || messageRoundtrip.hasToRetry()){

			Message msg = messageRoundtrip.getMsg();

			ctx.writeAndFlush(msg);

			if (msg.getAnswerMessage() == null)
				messageQueue.remove();
			else {
				messageRoundtrip.incRetryTimes();
				messageRoundtrip.saveTime();
			}
		}
	}

    public void close(){
        timer.purge();
    }
}

package org.ethereum.net;

import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.PingMessage;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.ethereum.net.message.StaticMessages.DISCONNECT_MESSAGE;

/**
 * This class contains the logic for sending messages in a queue
 *
 * Messages open by send and answered by receive of appropriate message
 *      PING by PONG
 *      GET_PEERS by PEERS
 *      GET_TRANSACTIONS by TRANSACTIONS
 *      GET_BLOCK_HASHES by BLOCK_HASHES
 *      GET_BLOCKS by BLOCKS
 *
 * The following messages will not be answered:
 *      PONG, PEERS, HELLO, STATUS, TRANSACTIONS, BLOCKS
 *
 * @author Roman Mandeleil
 */
@Component
@Scope("prototype")
public class MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private Queue<MessageRoundtrip> messageQueue = new ConcurrentLinkedQueue<>();
    private ChannelHandlerContext ctx = null;
    private final Timer timer = new Timer("MessageQueue");

    @Autowired
    WorldManager worldManager;
    boolean hasPing = false;

    public MessageQueue() {
    }

    public void activate(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                nudgeQueue();
            }
        }, 10, 10);
    }

    public void sendMessage(Message msg) {
        if (msg instanceof PingMessage) {
            if (hasPing) return;
            hasPing = true;
        }

        messageQueue.add(new MessageRoundtrip(msg));
    }

    public void disconnect() {
        ctx.writeAndFlush(DISCONNECT_MESSAGE);
        ctx.close();
    }

    public void disconnect(ReasonCode reason) {
        DisconnectMessage msg = new DisconnectMessage(reason);
        ctx.writeAndFlush(msg);
        ctx.close();
    }

    public void receivedMessage(Message msg) throws InterruptedException {

        worldManager.getListener().trace("[Recv: " + msg + "]");

        if (messageQueue.peek() != null) {
            MessageRoundtrip messageRoundtrip = messageQueue.peek();
            Message waitingMessage = messageRoundtrip.getMsg();

            if (waitingMessage instanceof PingMessage) hasPing = false;

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

            EthereumListener listener = worldManager.getListener();
            listener.onSendMessage(msg);

            ctx.writeAndFlush(msg);

            if (msg.getAnswerMessage() == null)
                messageQueue.remove();
            else {
                messageRoundtrip.incRetryTimes();
                messageRoundtrip.saveTime();
            }
        }
    }

    public void close() {
        timer.cancel();
        timer.purge();
    }
}

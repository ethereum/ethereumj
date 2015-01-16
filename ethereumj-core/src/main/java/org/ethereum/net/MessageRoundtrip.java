package org.ethereum.net;

import org.ethereum.net.message.Message;

/**
 * Utility wraps around a message to keep track of the number of times it has
 * been offered This class also contains the last time a message was offered and
 * is updated when an answer has been received to it can be removed from the
 * queue.
 *
 * @author Roman Mandeleil
 */
public class MessageRoundtrip {

    private final Message msg;
    long lastTimestamp = 0;
    long retryTimes = 0;
    boolean answered = false;

    public MessageRoundtrip(Message msg) {
        this.msg = msg;
        saveTime();
    }

    public boolean isAnswered() {
        return answered;
    }

    public void answer() {
        answered = true;
    }

    public long getRetryTimes() {
        return retryTimes;
    }

    public void incRetryTimes() {
        ++retryTimes;
    }

    public void saveTime() {
        lastTimestamp = System.currentTimeMillis();
    }

    public boolean hasToRetry() {
        return 20000 < System.currentTimeMillis() - lastTimestamp;
    }

    public Message getMsg() {
        return msg;
    }
}

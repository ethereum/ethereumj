package org.ethereum.net;

import org.ethereum.net.message.Message;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 09/07/2014 13:54
 */

public class MessageRoundtrip {

    Message msg = null;
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

    public void answer(){
        answered = true;
    }

    public long getRetryTimes() {
        return retryTimes;
    }

    public void incRetryTimes(){
        ++retryTimes;
    }
    public void saveTime(){lastTimestamp = System.currentTimeMillis();}

    public boolean hasToRetry(){
        return 5000 < System.currentTimeMillis() - lastTimestamp;
    }

    public Message getMsg() {
        return msg;
    }

}

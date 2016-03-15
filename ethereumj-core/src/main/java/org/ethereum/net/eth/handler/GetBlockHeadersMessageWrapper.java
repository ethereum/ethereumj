package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.message.GetBlockHeadersMessage;

/**
 * Wraps {@link GetBlockHeadersMessage},
 * adds some additional info required by get headers queue
 *
 * @author Mikhail Kalinin
 * @since 16.02.2016
 */
public class GetBlockHeadersMessageWrapper {

    private GetBlockHeadersMessage message;
    private boolean newHashesHandling = false;
    private boolean sent = false;

    public GetBlockHeadersMessageWrapper(GetBlockHeadersMessage message) {
        this.message = message;
    }

    public GetBlockHeadersMessageWrapper(GetBlockHeadersMessage message, boolean newHashesHandling) {
        this.message = message;
        this.newHashesHandling = newHashesHandling;
    }

    public GetBlockHeadersMessage getMessage() {
        return message;
    }

    public boolean isNewHashesHandling() {
        return newHashesHandling;
    }

    public boolean isSent() {
        return sent;
    }

    public void send() {
        this.sent = true;
    }
}

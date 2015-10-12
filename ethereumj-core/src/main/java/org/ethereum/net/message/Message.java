package org.ethereum.net.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract message class for all messages on the Ethereum network
 *
 * @author Roman Mandeleil
 * @since 06.04.14
 */
public abstract class Message {

    protected static final Logger logger = LoggerFactory.getLogger("net");

    protected boolean parsed;
    protected byte[] encoded;
    protected byte code;

    public Message() {
    }

    public Message(byte[] encoded) {
        this.encoded = encoded;
        parsed = false;
    }

    /**
     * Gets the RLP encoded byte array of this message
     *
     * @return RLP encoded byte array representation of this message
     */
    public abstract byte[] getEncoded();

    public abstract Class<?> getAnswerMessage();

    /**
     * Returns the message in String format
     *
     * @return A string with all attributes of the message
     */
    public abstract String toString();

    public abstract Enum getCommand();

    public byte getCode() {
            return code;
    }

}

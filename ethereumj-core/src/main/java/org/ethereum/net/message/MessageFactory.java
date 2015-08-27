package org.ethereum.net.message;

/**
 * Factory interface to create messages
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public interface MessageFactory {

    /**
     * Creates message by absolute message codes
     * e.g. codes described in {@link org.ethereum.net.eth.message.EthMessageCodes}
     *
     * @param code message code
     * @param encoded encoded message bytes
     * @return created message
     *
     * @throws IllegalArgumentException if code is unknown
     */
    Message create(byte code, byte[] encoded);

}

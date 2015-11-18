package org.ethereum.net;

import org.ethereum.net.message.Message;
import org.ethereum.net.message.ReasonCode;

/**
 * Protocol handler events
 *
 * @author Tiberius Iliescu
 */
public interface ProtocolHandlerListener {

    void onRemoteDisconnect(String protocol, Message disconnectMessage);
    void onLocalDisconnect(String protocol, ReasonCode reason);
    void onProtocolActivated(String protocolName, ProtocolHandler protocolHandler);
    void onProtocolMessageReceived(String protocolName, Message message);
    void onProtocolMessageSent(String protocolName, Message message);
}

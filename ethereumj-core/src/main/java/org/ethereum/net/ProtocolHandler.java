package org.ethereum.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.net.message.Message;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.message.ReasonCode;
import org.ethereum.net.rlpx.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Common protocol handler functionality
 *
 * @author Tiberius Iliescu
 */
public class ProtocolHandler<T> extends SimpleChannelInboundHandler<T> {

    protected final static Logger loggerNet = LoggerFactory.getLogger("net");
    protected final static Logger loggerSync = LoggerFactory.getLogger("sync");

    protected ArrayList<ProtocolHandlerListener> listeners = new ArrayList<>();

    protected MessageFactory messageFactory;

    protected MessageQueue messageQueue;

    protected String name;

    protected Node node;

    protected HashMap<String, ProtocolHandler> subProtocols = new HashMap<>();

    protected ProtocolHandler() {
        super();
    }

    protected ProtocolHandler(Class<? extends T> inboundMessageType) {
        super(inboundMessageType);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {

    }

    public void addListener(ProtocolHandlerListener listener) {

        this.listeners.add(listener);
        for (ProtocolHandler protocol: subProtocols.values()) {
            protocol.addListener(listener);
        }
    }

    public void addListeners(ArrayList<ProtocolHandlerListener> listeners) {

        for (ProtocolHandlerListener listener: listeners) {
            addListener(listener);
        }
    }

    public void removeListener(ProtocolHandlerListener listener) {

        this.listeners.remove(listener);
    }

    public void setMessageQueue(MessageQueue messageQueue) {

        this.messageQueue = messageQueue;
    }

    public void setNode(Node node) {

        this.node = node;
    }

    public String getPeerIdShort() {

        return node.getHexIdShort();
    }

    public byte[] getNodeId() {

        return node.getId();
    }

    protected void activateSubProtocol(ChannelHandlerContext ctx, String protocolName, ProtocolHandler protocolHandler) {

        subProtocols.put(protocolName, protocolHandler);
        protocolHandler.setMessageQueue(messageQueue);
        protocolHandler.setNode(node);
        protocolHandler.addListeners(listeners);
        protocolHandler.activate(protocolName);
        ctx.pipeline().addLast(protocolName, protocolHandler);
    }

    public void activate(String name) {

        this.name = name;
        onProtocolActivated(this);
    }

    public ProtocolHandler getSubProtocol(String protocolName) {

        return subProtocols.containsKey(protocolName) ? subProtocols.get(protocolName) : null;
    }

    public boolean hasCommand(Enum msgCommand) {

        return false;
    }

    public byte getCommandCode(Enum msgCommand) {

        return 0;
    }

    public boolean hasCommandCode(byte code) {

        return false;
    }

    public byte getMaxCommandCode() {

        return 0;
    }

    public Message createMessage(byte code, byte[] payload) {

        return messageFactory != null ? messageFactory.create(code, payload) : null;
    }

    // Events

    protected void onProtocolActivated(ProtocolHandler protocolHandler) {

        for (ProtocolHandlerListener listener: listeners) {
            listener.onProtocolActivated(name, protocolHandler);
        }
    }

    protected void onMessageReceived(Message message) {

        for (ProtocolHandlerListener listener: listeners) {
            listener.onProtocolMessageReceived(name, message);
        }
    }

    protected void onMessageSent(Message message) {

        for (ProtocolHandlerListener listener: listeners) {
            listener.onProtocolMessageSent(name, message);
        }
    }

    protected void onLocalDisconnect(ReasonCode reason) {

        for (ProtocolHandlerListener listener: listeners) {
            listener.onLocalDisconnect(name, reason);
        }
    }

    protected void onRemoteDisconnect(Message message) {

        for (ProtocolHandlerListener listener: listeners) {
            listener.onRemoteDisconnect(name, message);
        }
    }
}

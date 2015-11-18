package org.ethereum.net.rlpx;

import io.netty.channel.ChannelHandlerContext;
import org.ethereum.net.message.*;
import org.ethereum.net.p2p.DisconnectMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.spongycastle.math.ec.ECPoint;

/**
 * MessageCodec events
 *
 * @author Tiberius Iliescu
 */
public interface MessageCodecListener {

    void onMessageDecoded(org.ethereum.net.message.Message message);
    void onMessageEncoded(org.ethereum.net.message.Message message);
    void onRLPxHandshakeFinished(ChannelHandlerContext ctx, HelloMessage helloMessage);
    void onRLPxDisconnect(DisconnectMessage message);
    void onMessageCodecException(ChannelHandlerContext ctx, Throwable cause);
    void onHelloMessageSent(HelloMessage message);
    void onHelloMessageReceived(HelloMessage message);
    void onAuthentificationRequest(AuthInitiateMessage initiateMessage, ECPoint remotePubKey);
}


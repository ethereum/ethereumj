/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.net.swarm.bzz;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.swarm.NetStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Process the messages between peers with 'bzz' capability on the network.
 */
@Component
@Scope("prototype")
public class BzzHandler extends SimpleChannelInboundHandler<BzzMessage>
        implements Consumer<BzzMessage> {

    public final static byte VERSION = 0;
    private MessageQueue msgQueue = null;

    private boolean active = false;

    private final static Logger logger = LoggerFactory.getLogger("net");

    BzzProtocol bzzProtocol;

    @Autowired
    EthereumListener ethereumListener;

    @Autowired
    NetStore netStore;

    public BzzHandler() {
    }

    public BzzHandler(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, BzzMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (BzzMessageCodes.inRange(msg.getCommand().asByte()))
            logger.debug("BzzHandler invoke: [{}]", msg.getCommand());

        ethereumListener.trace(String.format("BzzHandler invoke: [%s]", msg.getCommand()));

        if (bzzProtocol != null) {
            bzzProtocol.accept(msg);
        }
    }

    @Override
    public void accept(BzzMessage bzzMessage) {
        msgQueue.sendMessage(bzzMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Bzz handling failed", cause);
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        active = false;
        logger.debug("handlerRemoved: ... ");
    }

    public void activate() {
        logger.info("BZZ protocol activated");
        ethereumListener.trace("BZZ protocol activated");
        createBzzProtocol();
        this.active = true;
    }

    private void createBzzProtocol() {
        bzzProtocol = new BzzProtocol(netStore /*NetStore.getInstance()*/);
        bzzProtocol.setMessageSender(this);
        bzzProtocol.start();
    }

    public boolean isActive() {
        return active;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }
}
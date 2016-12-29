package org.ethereum.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.FindNodeMessage;
import org.ethereum.net.rlpx.Message;
import org.ethereum.net.rlpx.discover.DiscoveryEvent;
import org.ethereum.net.rlpx.discover.PacketDecoder;
import org.ethereum.util.Functional;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.ethereum.util.ByteUtil.longToBytesNoLeadZeroes;

/**
 * Not for regular run, but just for testing UDP client/server communication
 *
 * For running server with gradle:
 * - adjust constants
 * - remove @Ignore from server() method
 * - > ./gradlew -Dtest.single=UdpTest test
 *
 * Created by Anton Nashatyrev on 28.12.2016.
 */
public class UdpTest {

    private static final String clientAddr = bindIp();
    private static final int clientPort = 8888;
    private static final String serverAddr = bindIp();
    private static final int serverPort = 30321;

    private static final String privKeyStr = "abb51256c1324a1350598653f46aa3ad693ac3cf5d05f36eba3f495a1f51590f";
    private static final ECKey privKey = ECKey.fromPrivate(Hex.decode(privKeyStr));
    private static final String defaultMessage = "rpuynovsmadskkegfuomewujbjdlcrfvhbvurqodrrvdbbjgwavcwplxyqkdirypsulkmbpfsnivqueouzrsoesovcrfusuomhaaiqgcdoztkdieezzufrjavcfrubiikijsyrwdhwzsbjygkxcpolqmqxfkbnvlwegwvcacazucitissmjjyzvqzaaaovnmbtiwyibyjxatsknlxxuxypnxrpzhnntoicngmqfwnhpgudtranqyedyxuxfhmyyvrpuynovsmadskkegfuomewujbjdlcrfvhbvurqodrrvdbbjgwavcwplxyqkdirypsulkmbpfsnivqueouzrsoesovcrfusuomhaaiqgcdoztkdieezzufrjavcfrubiikijsyrwdhwzsbjygkxcpolqmqxfkbnvlwegwvcacazucitissmjjyzvqzaaaovnmbtiwyibyjxatsknlxxuxypnxrpzhnntoicngmqfwnhpgudtranqyedyxuxfhmyyvrpuynovsmadskkegfuomewujbjdlcrfvhbvurqodrrvdbbjgwavcwplxyqkdirypsulkmbpfsnivqueouzrsoesovcrfusuomhaaiqgcdoztkdieezzufrjavcfrubiikijsyrwdhwzsbjygkxcpolqmqxfkbnvlwegwvcacazucitissmjjyzvqzaaaovnmbtiwyibyjxatsknlxxuxypnxrpzhnntoicngmqfwnhpgudtranqyedyxuxfhmyyvrpuynovsmadskkegfuomewujbjdlcrfvhbvurqodrrvdbbjgwavcwplxyqkdirypsulkmbpfsnivqueouzrsoesovcrfusuomhaaiqgcdoztkdieezzufrjavcfrubiikijsyrwdhwzsbjygkxcpolqmqxfkbnvlwegwvcacazucitissmjjyzvqzaaaovnmbtiwyibyjxatsknlxxuxypnxrpzhnntoicngmqfwnhpgudtranqyedyxuxfhmyyvHello from client";

    private final SimpleNodeManager nodeManager = new SimpleNodeManager();

    private class SimpleMessageHandler extends SimpleChannelInboundHandler<DiscoveryEvent>
            implements Functional.Consumer<DiscoveryEvent>  {

        Channel channel;

        SimpleNodeManager nodeManager;

        public SimpleMessageHandler(Channel channel, SimpleNodeManager nodeManager) {
            this.channel = channel;
            this.nodeManager = nodeManager;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
            System.out.printf("Channel initialized on %s:%s%n", localAddress.getHostString(), localAddress.getPort());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DiscoveryEvent msg) throws Exception {
            InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
            System.out.printf("Message received on %s:%s%n", localAddress.getHostString(), localAddress.getPort());
            nodeManager.handleInbound(msg);
        }

        @Override
        public void accept(DiscoveryEvent discoveryEvent) {
            InetSocketAddress address = discoveryEvent.getAddress();
            sendPacket(discoveryEvent.getMessage().getPacket(), address);
        }

        private void sendPacket(byte[] payload, InetSocketAddress address) {
            DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(payload), address);
            System.out.println("Sending message from " + clientAddr + ":" + clientPort +
                    " to " + address.getHostString() + ":" + address.getPort());
            channel.writeAndFlush(packet);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }

    private class SimpleNodeManager {

        SimpleMessageHandler messageSender;

        public void setMessageSender(SimpleMessageHandler messageSender) {
            this.messageSender = messageSender;
        }

        public SimpleMessageHandler getMessageSender() {
            return messageSender;
        }

        public void handleInbound(DiscoveryEvent discoveryEvent) {
            Message m = discoveryEvent.getMessage();
            if (!(m instanceof FindNodeMessage)) {
                return;
            }
            String msg = new String(((FindNodeMessage) m).getTarget());
            System.out.printf("Inbound message \"%s\" from %s:%s%n", msg,
                    discoveryEvent.getAddress().getHostString(), discoveryEvent.getAddress().getPort());
            if (msg.endsWith("+1")) {
                messageSender.channel.close();
            } else {
                FindNodeMessage newMsg = FindNodeMessage.create((msg + "+1").getBytes(), privKey);
                messageSender.sendPacket(newMsg.getPacket(), discoveryEvent.getAddress());
            }
        }
    }

    public Channel create(String bindAddr, int port) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioDatagramChannel.class)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                public void initChannel(NioDatagramChannel ch)
                        throws Exception {
                    ch.pipeline().addLast(new PacketDecoder());
                    SimpleMessageHandler messageHandler = new SimpleMessageHandler(ch, nodeManager);
                    nodeManager.setMessageSender(messageHandler);
                    ch.pipeline().addLast(messageHandler);
                }
            });

        return b.bind(bindAddr, port).sync().channel();
    }

    public void startServer() throws InterruptedException {
        create(serverAddr, serverPort).closeFuture().sync();
    }

    public void startClient()
            throws InterruptedException {
        for (int i = defaultMessage.length() - 1; i >= 0 ; i--) {
            Channel channel = create(clientAddr, clientPort);
            String sendMessage = defaultMessage.substring(i, defaultMessage.length());
            System.out.printf("Sending message with string payload of size %s%n", sendMessage.length());
            FindNodeMessage msg = FindNodeMessage.create(sendMessage.getBytes(), privKey);
            nodeManager.getMessageSender().sendPacket(msg.getPacket(), new InetSocketAddress(serverAddr, serverPort));
            boolean ok = channel.closeFuture().await(10, TimeUnit.SECONDS);
            if (!ok) {
                System.out.println("ERROR: Timeout waiting for response");
                assert false;
            } else {
                System.out.println("OK");
            }
        }
    }

    @Ignore
    @Test
    public void server() throws Exception {
        startServer();
    }

    @Ignore
    @Test
    public void client() throws Exception {
        startClient();
    }

    public static String bindIp() {
        String bindIp;
            try {
                Socket s = new Socket("www.google.com", 80);
                bindIp = s.getLocalAddress().getHostAddress();
                System.out.printf("UDP local bound to: %s%n", bindIp);
            } catch (IOException e) {
                System.out.printf("Can't get bind IP. Fall back to 0.0.0.0: " + e);
                bindIp = "0.0.0.0";
            }
        return bindIp;
    }
}

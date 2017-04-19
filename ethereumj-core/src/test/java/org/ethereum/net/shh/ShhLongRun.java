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
package org.ethereum.net.shh;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.NoAutoscan;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This is not a JUnit test but rather a long running standalone test for messages exchange with another peer.
 * To start it another peer with JSON PRC API should be started.
 * E.g. the following is the cmd for starting up C++ eth:
 *
 * > eth --no-bootstrap --no-discovery --listen 10003 --shh --json-rpc
 *
 * If the eth is running on a remote host:port the appropriate constants in the test need to be updated
 *
 * Created by Anton Nashatyrev on 05.10.2015.
 */
@Ignore
public class ShhLongRun extends Thread {
    private static URL remoteJsonRpc;

    @Test
    public void test() throws Exception {
//        remoteJsonRpc = new URL("http://whisper-1.ether.camp:8545");
//        Node node = new Node("enode://52994910050f13cbd7848f02709f2f5ebccc363f13dafc4ec201e405e2f143ebc9c440935b3217073f6ec47f613220e0bc6b7b34274b7d2de125b82a2acd34ee" +
//                "@whisper-1.ether.camp:30303");

        remoteJsonRpc = new URL("http://localhost:8545");
        Node node = new Node("enode://6ed738b650ac2b771838506172447dc683b7e9dae7b91d699a48a0f94651b1a0d2e2ef01c6fffa22f762aaa553286047f0b0bb39f2e3a24b2a18fe1b9637dcbe" +
                "@localhost:10003");

        Ethereum ethereum = EthereumFactory.createEthereum(Config.class);
        ethereum.connect(
                node.getHost(),
                node.getPort(),
                Hex.toHexString(node.getId()));
        Thread.sleep(1000000000);
    }

    public static void main(String[] args) throws Exception {
        new ShhLongRun().test();
    }

    @Configuration
    @NoAutoscan
    public static class Config {

        @Bean
        public TestComponent testBean() {
            return new TestComponent();
        }
    }

    @Component
    @NoAutoscan
    public static class TestComponent extends Thread {

        @Autowired
        WorldManager worldManager;

        @Autowired
        Ethereum ethereum;


        @Autowired
        Whisper whisper;

        Whisper remoteWhisper;

        public TestComponent() {
        }

        @PostConstruct
        void init() {
            System.out.println("========= init");
            worldManager.addListener(new EthereumListenerAdapter() {
                @Override
                public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {
                    System.out.println("========= onHandShakePeer");
                    if (!isAlive()) {
                        start();
                    }
                }
            });
        }

        static class MessageMatcher extends MessageWatcher {
            List<Pair<Date, WhisperMessage>> awaitedMsgs = new ArrayList<>();

            public MessageMatcher(String to, String from, Topic[] topics) {
                super(to, from, topics);
            }

            @Override
            protected synchronized void newMessage(WhisperMessage msg) {
                System.out.println("=== Msg received: " + msg);
                for (Pair<Date, WhisperMessage> awaitedMsg : awaitedMsgs) {
                    if (Arrays.equals(msg.getPayload(), awaitedMsg.getRight().getPayload())) {
                        if (!match(msg, awaitedMsg.getRight())) {
                            throw new RuntimeException("Messages not matched: \n" + awaitedMsg + "\n" + msg);
                        } else {
                            awaitedMsgs.remove(awaitedMsg);
                            break;
                        }
                    }
                }
                checkForMissingMessages();
            }

            private boolean equal(Object o1, Object o2) {
                if (o1 == null) return o2 == null;
                return o1.equals(o2);
            }

            protected boolean match(WhisperMessage m1, WhisperMessage m2) {
                if (!Arrays.equals(m1.getPayload(), m2.getPayload())) return false;
                if (!equal(m1.getFrom(), m2.getFrom())) return false;
                if (!equal(m1.getTo(), m2.getTo())) return false;
                if (m1.getTopics() != null) {
                    if (m1.getTopics().length != m2.getTopics().length) return false;
                    for (int i = 0; i < m1.getTopics().length; i++) {
                        if (!m1.getTopics()[i].equals(m2.getTopics()[i])) return false;
                    }
                } else if (m2.getTopics() != null) return false;
                return true;
            }

            public synchronized void waitForMessage(WhisperMessage msg) {
                checkForMissingMessages();
                awaitedMsgs.add(Pair.of(new Date(), msg));
            }

            private void checkForMissingMessages() {
                for (Pair<Date, WhisperMessage> msg : awaitedMsgs) {
                    if (System.currentTimeMillis() > msg.getLeft().getTime() + 10 * 1000) {
                        throw new RuntimeException("Message was not delivered: " + msg);
                    }
                }
            }
        }

        @Override
        public void run() {

            try {
                remoteWhisper = new JsonRpcWhisper(remoteJsonRpc);
                Whisper whisper = this.whisper;
                //            Whisper whisper = new JsonRpcWhisper(remoteJsonRpc1);

                System.out.println("========= Waiting for SHH init");
                Thread.sleep(1 * 1000);

                System.out.println("========= Running");


                String localKey1 = whisper.newIdentity();
                String localKey2 = whisper.newIdentity();
                String remoteKey1 = remoteWhisper.newIdentity();
                String remoteKey2 = remoteWhisper.newIdentity();

                String localTopic = "LocalTopic";
                String remoteTopic = "RemoteTopic";

                MessageMatcher localMatcherBroad = new MessageMatcher(null, null, Topic.createTopics(remoteTopic));
                MessageMatcher remoteMatcherBroad = new MessageMatcher(null, null, Topic.createTopics(localTopic));
                MessageMatcher localMatcherTo = new MessageMatcher(localKey1, null, null);
                MessageMatcher localMatcherToFrom = new MessageMatcher(localKey2, remoteKey2, null);
                MessageMatcher remoteMatcherTo = new MessageMatcher(remoteKey1, null, Topic.createTopics("aaa"));
                MessageMatcher remoteMatcherToFrom = new MessageMatcher(remoteKey2, localKey2, Topic.createTopics("aaa"));

                whisper.watch(localMatcherBroad);
                whisper.watch(localMatcherTo);
                whisper.watch(localMatcherToFrom);
                remoteWhisper.watch(remoteMatcherBroad);
                remoteWhisper.watch(remoteMatcherTo);
                remoteWhisper.watch(remoteMatcherToFrom);

                int cnt = 0;
                while (true) {
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("local-" + cnt)
                                .setTopics(Topic.createTopics(localTopic));
                        remoteMatcherBroad.waitForMessage(msg);
                        whisper.send(msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("remote-" + cnt)
                                .setTopics(Topic.createTopics(remoteTopic));
                        localMatcherBroad.waitForMessage(msg);
                        remoteWhisper.send(msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("local-to-" + cnt)
                                .setTo(remoteKey1)
                                .setTopics(Topic.createTopics("aaa"));
                        remoteMatcherTo.waitForMessage(msg);
                        whisper.send(msg.getTo(), msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("remote-to-" + cnt)
                                .setTo(localKey1);
                        localMatcherTo.waitForMessage(msg);
                        remoteWhisper.send(msg.getTo(), msg.getPayload(), Topic.createTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("local-to-from-" + cnt)
                                .setTo(remoteKey2)
                                .setFrom(localKey2)
                                .setTopics(Topic.createTopics("aaa"));
                        remoteMatcherToFrom.waitForMessage(msg);
                        whisper.send(msg.getFrom(), msg.getTo(), msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("remote-to-from-" + cnt)
                                .setTo(localKey2)
                                .setFrom(remoteKey2);
                        localMatcherToFrom.waitForMessage(msg);
                        remoteWhisper.send(msg.getFrom(), msg.getTo(), msg.getPayload(), msg.getTopics());
                    }

                    Thread.sleep(1000);
                    cnt++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

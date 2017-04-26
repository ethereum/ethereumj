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


import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class WhisperImpl extends Whisper {
    private final static Logger logger = LoggerFactory.getLogger("net.shh");

    private Set<MessageWatcher> filters = new HashSet<>();
    private List<Topic> knownTopics = new ArrayList<>();

    private Map<WhisperMessage, ?> known = new LRUMap<>(1024); // essentially Set

    private Map<String, ECKey> identities = new HashMap<>();

    private List<ShhHandler> activePeers = new ArrayList<>();

    BloomFilter hostBloomFilter = BloomFilter.createAll();

    public WhisperImpl() {
    }

    @Override
    public void send(String from, String to, byte[] payload, Topic[] topicList, int ttl, int workToProve) {
        ECKey fromKey = null;
        if (from != null && !from.isEmpty()) {
            fromKey = getIdentity(from);
            if (fromKey == null) {
                throw new Error(String.format("Unknown identity to send from %s", from));
            }
        }

        WhisperMessage m = new WhisperMessage()
                .setFrom(fromKey)
                .setTo(to)
                .setPayload(payload)
                .setTopics(topicList)
                .setTtl(ttl)
                .setWorkToProve(workToProve);

        logger.info("Sending Whisper message: " + m);

        addMessage(m, null);
    }

    public void processEnvelope(ShhEnvelopeMessage e, ShhHandler shhHandler) {
        for (WhisperMessage message : e.getMessages()) {
            message.decrypt(identities.values(), knownTopics);
            logger.info("New Whisper message: " + message);
            addMessage(message, shhHandler);
        }
    }

    void addPeer(ShhHandler peer) {
        activePeers.add(peer);
    }

    void removePeer(ShhHandler peer) {
        activePeers.remove(peer);
    }

    public void watch(MessageWatcher f) {
        filters.add(f);
        for (Topic topic : f.getTopics()) {
            hostBloomFilter.addTopic(topic);
            knownTopics.add(topic);
        }
        notifyBloomFilterChanged();
    }

    public void unwatch(MessageWatcher f) {
        filters.remove(f);
        for (Topic topic : f.getTopics()) {
            hostBloomFilter.removeTopic(topic);
        }
        notifyBloomFilterChanged();
    }

    private void notifyBloomFilterChanged() {
        for (ShhHandler peer : activePeers) {
            peer.sendHostBloom();
        }
    }

    // Processing both messages:
    // own outgoing messages (shhHandler == null)
    // and inbound messages from peers
    private void addMessage(WhisperMessage m, ShhHandler inboundPeer) {
        if (!known.containsKey(m)) {
            known.put(m, null);
            if (inboundPeer != null) {
                matchMessage(m);
            }

            for (ShhHandler peer : activePeers) {
                if (peer != inboundPeer) {
                    peer.sendEnvelope(new ShhEnvelopeMessage(m));
                }
            }
        }
    }

    private void matchMessage(WhisperMessage m) {
        for (MessageWatcher f : filters) {
            if (f.match(m.getTo(), m.getFrom(), m.getTopics())) {
                f.newMessage(m);
            }
        }
    }

    public static String toIdentity(ECKey key) {
        return Hex.toHexString(key.getNodeId());
    }

    public static ECKey fromIdentityToPub(String identity) {
        try {
            return identity == null ? null :
                    ECKey.fromPublicOnly(ByteUtil.merge(new byte[] {0x04}, Hex.decode(identity)));
        } catch (Exception e) {
            throw new RuntimeException("Converting identity '" + identity + "'", e);
        }
    }

    @Override
    public String addIdentity(ECKey key) {
        String identity = toIdentity(key);
        identities.put(identity, key);
        return identity;
    }

    @Override
    public String newIdentity() {
        return addIdentity(new ECKey());
    }

    public ECKey getIdentity(String identity) {
        if (identities.containsKey(identity)) {
            return identities.get(identity);
        }

        return null;
    }
}

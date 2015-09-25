package org.ethereum.net.shh;


import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class WhisperImpl extends Whisper {
    private final static Logger logger = LoggerFactory.getLogger("net");

    private Set<MessageWatcher> filters = new HashSet<>();

    private Map<WhisperMessage, ?> known = new LRUMap<>(1024); // essentially Set

    private Map<String, ECKey> identities = new HashMap<>();

    private List<ShhHandler> activePeers = new ArrayList<>();

    BloomFilter hostBloomFilter = BloomFilter.createAll();

    public WhisperImpl() {
        addIdentity(SystemProperties.CONFIG.getMyKey());
    }

    @Override
    public void send(ECKey from, String to, byte[] payload, Topic[] topicList, int ttl, int workToProve) {
        WhisperMessage m = new WhisperMessage()
                .setFrom(from)
                .setTo(to == null ? null : Hex.decode(to))
                .setPayload(payload)
                .setTopics(topicList)
                .setTtl(ttl)
                .setNonce(workToProve);

        logger.info("Sending Whisper message: " + m);

        addMessage(m, null);
    }

    public void post(String from, String to, String[] topics, String payload, int ttl, int pow) {
        Topic[] topicList = Topic.createTopics(topics);

        ECKey key = null;
        if (from != null && !from.isEmpty()) {
            key = getIdentity(from);
            if (key == null) {
                throw new Error(String.format("Unknown identity to send from %s", from));
            }
        }

        send(key, to, payload.getBytes(StandardCharsets.UTF_8), topicList, ttl, pow);
    }

    public void processEnvelope(ShhEnvelopeMessage e, ShhHandler shhHandler) {
        for (WhisperMessage message : e.getMessages()) {
            boolean ok = false;
            for (ECKey key : identities.values()) {
                if (ok = message.decrypt(key)) break;
            }

            if (!ok) {
                // the message might be either not-encrypted or encrypted but we have no receivers
                // now way to know so just assuming that the message is broadcast and not encrypted
                message.setEncrypted(false);
            }
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

    @Override
    public void addIdentity(ECKey key) {
        identities.put(Hex.toHexString(key.getPubKey()), key);
    }

    @Override
    public ECKey newIdentity() {
        ECKey key = new ECKey().decompress();
        identities.put(Hex.toHexString(key.getPubKey()), key);
        return key;
    }

    public ECKey getIdentity(String keyHexString) {
        if (identities.containsKey(keyHexString)) {
            return identities.get(keyHexString);
        }

        return null;
    }
}

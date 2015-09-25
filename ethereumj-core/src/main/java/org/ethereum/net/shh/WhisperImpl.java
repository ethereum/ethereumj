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
        WhisperMessage m = new WhisperMessage(payload);

        ECKey key = null;

        Options options = new Options(
                key,
                Hex.decode(to),
                topicList,
                ttl
        );

        ShhEnvelopeMessage e = m.wrap(workToProve, options);

        logger.info("Sending Whisper message: " + m);

        addMessage(m, e, null);
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
        WhisperMessage m = open(e);
        if (m == null) {
            return;
        }

        logger.info("New Whisper message: " + m);
        addMessage(m, e, shhHandler);
    }

    private WhisperMessage open(ShhEnvelopeMessage e) {

        WhisperMessage m;

        for (ECKey key : identities.values()) {
            m = e.open(key);
            if (m != null) {
                return m;
            }
        }

        m = e.open(null);

        return m;
    }

    void sendMessage(ShhEnvelopeMessage e) {
        for (ShhHandler activePeer : activePeers) {
            activePeer.sendEnvelope(e);
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
    private void addMessage(WhisperMessage m, ShhEnvelopeMessage e, ShhHandler inboundPeer) {
        if (!known.containsKey(m)) {
            known.put(m, null);
            if (inboundPeer != null) {
                matchMessage(m, e.getTopics());
            }

            for (ShhHandler peer : activePeers) {
                if (peer != inboundPeer) {
                    peer.sendEnvelope(e);
                }
            }
        }
    }

    private void matchMessage(WhisperMessage m, Topic[] topics) {
        for (MessageWatcher f : filters) {
            if (f.match(m.getTo(), m.getPubKey(), topics)) {
                f.newMessage(m);
            }
        }
    }

    public void addIdentity(ECKey key) {
        identities.put(Hex.toHexString(key.getPubKey()), key);
    }

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

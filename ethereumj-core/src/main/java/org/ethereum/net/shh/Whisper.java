package org.ethereum.net.shh;


import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Whisper {
    private final static Logger logger = LoggerFactory.getLogger("net");

    private Set<Filter> filters = new HashSet<>();

    private Set<WhisperMessage> messages = new HashSet<>();
    private Set<WhisperMessage> known = new HashSet<>();

    private Map<String, ECKey> identities = new HashMap<>();

    private List<ShhHandler> activePeers = new ArrayList<>();

    public Whisper() {
        addIdentity(SystemProperties.CONFIG.getMyKey());
    }

    public void post(String from, String to, String[] topics, String payload, int ttl, int pow) {

        Topic[] topicList = Topic.createTopics(topics);

        WhisperMessage m = new WhisperMessage(payload.getBytes());

        ECKey key = null;

        if (from != null && !from.isEmpty()) {
            key = getIdentity(from);
            if (key == null) {
                throw new Error(String.format("Unknown identity to send from %s", from));
            }
        }

        Options options = new Options(
                key,
                Hex.decode(to),
                topicList,
                ttl
        );

        ShhEnvelopeMessage e = m.wrap(pow, options);

        logger.info("Sending Whisper message: " + m);

        addMessage(m, e.getTopics());
        sendMessage(e);
    }

    public void processEnvelope(ShhEnvelopeMessage e, ShhHandler shhHandler) {
        WhisperMessage m = open(e);
        if (m == null) {
            return;
        }

        logger.info("New Whisper message: " + m);
        addMessage(m, e.getTopics());
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
            activePeer.sendMessage(e);
        }
    }

    void addPeer(ShhHandler peer) {
        activePeers.add(peer);
    }

    void removePeer(ShhHandler peer) {
        activePeers.remove(peer);
    }

    public void watch(Filter f) {
        filters.add(f);
    }

    public void unwatch(Filter f) {
        filters.remove(f);
    }

    private void addMessage(WhisperMessage m, Topic[] topics) {
        known.add(m);
        matchMessage(m, topics);
    }

    public Filter createFilter(byte[] to, byte[] from, Topic[] topics) {
        TopicMatcher topicMatcher = new TopicMatcher(topics);
        Filter f = new Filter(to, from, topicMatcher);
        return f;
    }

    private void matchMessage(WhisperMessage m, Topic[] topics) {
        Filter msgFilter = createFilter(m.getTo(), m.getPubKey(), topics);
        for (Filter f : filters) {
            if (f.match(msgFilter)) {
                f.trigger();
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

    public List<WhisperMessage> getAllKnownMessages() {
        List<WhisperMessage> messageList = new ArrayList<>();
        messageList.addAll(known);
        return messageList;
    }

    public void setBloomFilter(ShhFilterMessage msg, ShhHandler shhHandler) {

    }
}

package org.ethereum.net.shh;


import org.ethereum.crypto.ECKey;
import org.ethereum.net.MessageQueue;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

public class Whisper {

    private MessageQueue msgQueue = null;

    private Set<Filter> filters = new HashSet<>();

    private Set<Message> messages = new HashSet<>();
    private Set<Message> known = new HashSet<>();

    private Map<String, ECKey> identities = new HashMap<>();

    public Whisper(MessageQueue messageQueue) {
        this.msgQueue = messageQueue;
        WhisperForm form = new WhisperForm(this);
    }

    public void post(String from, String to, String[] topics, String payload, int ttl, int pow) {

        Topic[] topicList = Topic.createTopics(topics);

        Message m = new Message(payload.getBytes());

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

        Envelope e = m.wrap(pow, options);

        addMessage(m);
        msgQueue.sendMessage(e);
    }

    public void processEnvelope(Envelope e) {
        Message m = open(e);
        if (m == null) {
            return;
        }
        addMessage(m);
    }

    private Message open(Envelope e) {

        Message m;

        for (ECKey key : identities.values()) {
            m = e.open(key);
            if (m != null) {
                return m;
            }
        }

        m = e.open(null);

        return m;
    }

    public void watch(Filter f) {
        filters.add(f);
    }

    public void unwatch(Filter f) {
        filters.remove(f);
    }

    private void addMessage(Message m) {
        known.add(m);
        matchMessage(m);
    }

    public Filter createFilter(byte[] from, byte[] to, String[] topics) {
        TopicMatcher topicMatcher = new TopicMatcher(topics);
        Filter f = new Filter(Hex.toHexString(from), Hex.toHexString(to), topicMatcher);
        return f;
    }

    //TODO: add filter matching
    private void matchMessage(Message m) {
        for (Filter f : filters) {
            if (f.matchMessage(m)) {
                break;
            }
        }
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

    public List<Message> getAllKnownMessages() {
        List<Message> messageList = new ArrayList<>();
        messageList.addAll(known);
        return messageList;
    }
 }

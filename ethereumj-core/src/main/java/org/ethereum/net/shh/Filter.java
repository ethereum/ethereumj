package org.ethereum.net.shh;


import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

public class Filter {
    private String to = "";
    private String from = "";
    private TopicMatcher filterTopics;

    public Filter(String to, String from, TopicMatcher filterTopics) {
        this.to = to;
        this.from = from;
        this.filterTopics = filterTopics;
    }

    public Filter(byte[] to, byte[] from, TopicMatcher filterTopics) {
        this.to = Hex.toHexString(to);
        this.from = Hex.toHexString(from);
        this.filterTopics = filterTopics;
    }

    public Filter(Message m, Topic[] topics) {
        byte[] to = m.getTo();
        if (to != null) {
            this.to = Hex.toHexString(to);
        }
        ECKey fromPubKey = m.recover();
        if (fromPubKey != null) {
            this.from = Hex.toHexString(fromPubKey.decompress().getPubKey());
        }

    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public TopicMatcher getFilterTopics() {
        return filterTopics;
    }

    public void setFilterTopics(TopicMatcher filterTopics) {
        this.filterTopics = filterTopics;
    }

    public boolean matchMessage(Message m) {

        return true;
    }

    private void trigger() {
        System.out.print("Trigger is called.");
    }
}

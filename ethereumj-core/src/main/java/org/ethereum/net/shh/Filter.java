package org.ethereum.net.shh;


public class Filter {
    private byte[] to;
    private byte[] from;
    private TopicMatcher filterTopics;

    public Filter(byte[] to, byte[] from, TopicMatcher filterTopics) {
        this.to = to;
        this.from = from;
        this.filterTopics = filterTopics;
    }

    public Filter(Message m, Topic[] topics) {
//        this.to = m.
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
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

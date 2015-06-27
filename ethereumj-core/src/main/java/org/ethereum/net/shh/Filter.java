package org.ethereum.net.shh;


import java.util.Arrays;
import java.util.List;

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
        this.to = m.getTo();
        this.from = m.getPubKey();
        this.filterTopics = new TopicMatcher(topics);
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

    public List<Topic> getTopics() {
        return filterTopics.getTopics();
    }

    public boolean match(Filter f) {

        if (this.to != null) {
            if (!Arrays.equals(this.to, f.getTo())) {
                return false;
            }
        }

        if (this.from != null) {
            if (!Arrays.equals(this.from, f.getFrom())) {
                return false;
            }
        }

        if (this.filterTopics != null) {
            Topic[] topics = new Topic[f.getTopics().size()];
            f.getTopics().toArray(topics);
            return this.filterTopics.matches(topics);
        }

        return true;
    }

    public void trigger() {
        System.out.print("Trigger is called.");
    }
}

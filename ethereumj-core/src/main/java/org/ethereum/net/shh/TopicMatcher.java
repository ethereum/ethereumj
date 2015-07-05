package org.ethereum.net.shh;

import java.util.ArrayList;
import java.util.List;

public class TopicMatcher {
    private List<List<Topic>> conditions = new ArrayList<>();

    public TopicMatcher(Topic[] inputTopics) {
        for (Topic t : inputTopics) {
            List<Topic> topics = new ArrayList<>();
            topics.add(t);
            conditions.add(topics);
        }
    }

    public TopicMatcher(String[] topicStrings) {
        for (String s : topicStrings) {
            List<Topic> topics = new ArrayList<>();
            for (String t : s.split(" ")) {
                topics.add(new Topic(t));
            }
            conditions.add(topics);
        }
    }

    public boolean matches(Topic[] topics) {
        if (conditions.size() > topics.length) {
            return false;
        }

        for (int i = 0; i < conditions.size() && i < topics.length; i++) {
            if (conditions.get(i).size() > 0) {
                if (!conditions.get(i).contains(topics[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();
        for (List<Topic> topicsList : conditions) {
            topics.addAll(topicsList);
        }
        return topics;
    }
}

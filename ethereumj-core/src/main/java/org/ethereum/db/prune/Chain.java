package org.ethereum.db.prune;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A single chain in a blockchain {@link Segment}.
 * It could represent either fork or the main chain.
 *
 * <p>
 *     Chain consists of certain number of {@link ChainItem}
 *     connected to each other with inheritance
 *
 * @author Mikhail Kalinin
 * @since 24.01.2018
 */
public class Chain {

    static final Chain NULL = new Chain() {
        @Override
        boolean connect(ChainItem item) {
            throw new RuntimeException("Not supported for null chain");
        }
    };

    List<ChainItem> items = new ArrayList<>();

    public List<byte[]> getHashes() {
        return items.stream().map(item -> item.hash).collect(Collectors.toList());
    }

    private Chain() {
    }

    Chain(ChainItem item) {
        this.items.add(item);
    }

    ChainItem top() {
        return items.size() > 0 ? items.get(items.size() - 1) : null;
    }

    long topNumber() {
        return top() != null ? top().number : 0;
    }

    boolean isHigher(Chain other) {
        return other.topNumber() < this.topNumber();
    }

    boolean contains(ChainItem other) {
        for (ChainItem item : items) {
            if (item.equals(other))
                return true;
        }
        return false;
    }

    boolean connect(ChainItem item) {
        if (top().isParentOf(item)) {
            items.add(item);
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        if (items.isEmpty()) {
            return "(empty)";
        }
        return "[" + items.get(0) +
                " ~> " + items.get(items.size() - 1) +
                ']';
    }

}

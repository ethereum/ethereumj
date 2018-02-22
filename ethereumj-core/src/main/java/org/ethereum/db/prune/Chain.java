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

    long startNumber() {
        return items.isEmpty() ? 0 : items.get(0).number;
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

    static Chain fromItems(ChainItem ... items) {
        if (items.length == 0) {
            return NULL;
        }

        Chain chain = null;
        for (ChainItem item : items) {
            if (chain == null) {
                chain = new Chain(item);
            } else {
                chain.connect(item);
            }
        }

        return chain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chain chain = (Chain) o;

        return !(items != null ? !items.equals(chain.items) : chain.items != null);
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

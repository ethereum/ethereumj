package org.ethereum.db.prune;

import org.ethereum.core.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an interface for building and tracking chain segment.
 *
 * <p>
 *     Chain segment is a fragment of the blockchain, it includes both forks and main chain.
 *     Segment always has a 'root' item which must belong to the main chain,
 *     anyway 'root' item itself is not treated as a part of the segment.
 *
 * <p>
 *     Segment is complete when its main chain top item is the highest (fork tops have lower numbers).
 *     Whether segment is complete or not can be checked by call to {@link #isComplete()}
 *
 * <p>
 *     Segment has a {@link Tracker} class which helps to update segment with new blocks.
 *     Its Usage is simple: add all blocks with {@link Tracker#addAll(List)},
 *     add main chain blocks with {@link Tracker#addMain(Block)},
 *     then when all blocks are added {@link Tracker#commit()} should be fired
 *     to connect added blocks to the segment
 *
 * @author Mikhail Kalinin
 * @since 24.01.2018
 *
 * @see Chain
 * @see ChainItem
 */
public class Segment {

    List<Chain> forks = new ArrayList<>();
    Chain main = Chain.NULL;
    ChainItem root;

    public Segment(Block root) {
        this.root = new ChainItem(root);
    }

    public Segment(long number, byte[] hash, byte[] parentHash) {
        this.root = new ChainItem(number, hash, parentHash);
    }

    public boolean isComplete() {
        if (main == Chain.NULL)
            return false;

        for (Chain fork : forks) {
            if (!main.isHigher(fork))
                return false;
        }
        return true;
    }

    public long getRootNumber() {
        return root.number;
    }

    public long getMaxNumber() {
        return main.topNumber();
    }

    public Tracker startTracking() {
        return new Tracker(this);
    }

    public int size() {
        return main.items.size();
    }

    private void branch(ChainItem item) {
        forks.add(new Chain(item));
    }

    private void connectMain(ChainItem item) {
        if (main == Chain.NULL) {
            if (root.isParentOf(item))
                main = new Chain(item); // start new
        } else {
            main.connect(item);
        }
    }

    private void connectFork(ChainItem item) {

        for (Chain fork : forks) {
            if (fork.contains(item))
                return;
        }

        if (root.isParentOf(item)) {
            branch(item);
        } else {
            for (ChainItem mainItem : main.items) {
                if (mainItem.isParentOf(item)) {
                    branch(item);
                }
            }

            for (Chain fork : forks) {
                if (fork.connect(item)) {
                    return;
                }
            }

            List<Chain> branchedForks = new ArrayList<>();
            for (Chain fork : forks) {
                for (ChainItem forkItem : fork.items) {
                    if (forkItem.isParentOf(item)) {
                        branchedForks.add(new Chain(item));
                    }
                }
            }
            forks.addAll(branchedForks);
        }
    }

    @Override
    public String toString() {
        return "" + main;
    }

    public static final class Tracker {

        Segment segment;
        List<ChainItem> main = new ArrayList<>();
        List<ChainItem> items = new ArrayList<>();

        Tracker(Segment segment) {
            this.segment = segment;
        }

        public void addMain(Block block) {
            main.add(new ChainItem(block));
        }

        public void addAll(List<Block> blocks) {
            items.addAll(blocks.stream()
                    .map(ChainItem::new)
                    .collect(Collectors.toList()));
        }

        public Tracker addMain(long number, byte[] hash, byte[] parentHash) {
            main.add(new ChainItem(number, hash, parentHash));
            return this;
        }

        public Tracker addItem(long number, byte[] hash, byte[] parentHash) {
            items.add(new ChainItem(number, hash, parentHash));
            return this;
        }

        public void commit() {

            items.removeAll(main);

            main.sort((i1, i2) -> Long.compare(i1.number, i2.number));
            items.sort((i1, i2) -> Long.compare(i1.number, i2.number));

            main.forEach(segment::connectMain);
            items.forEach(segment::connectFork);
        }
    }
}

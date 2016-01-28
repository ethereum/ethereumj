package org.ethereum.db.index;

import java.util.Collection;

/**
 * @author Mikhail Kalinin
 * @since 28.01.2016
 */
public interface Index extends Iterable<Long> {

    void addAll(Collection<Long> nums);

    void add(Long num);

    Long peek();

    Long poll();

    boolean contains(Long num);

    boolean isEmpty();

    int size();

    void clear();

    void removeAll(Collection<Long> indexes);
}

package org.ethereum.db.index;

import java.util.*;

/**
 * @author Mikhail Kalinin
 * @since 28.01.2016
 */
public class ArrayListIndex implements Index {
    private List<Long> index;

    public ArrayListIndex(Collection<Long> numbers) {
        index = new ArrayList<>(numbers);
        sort();
    }

    @Override
    public synchronized void addAll(Collection<Long> nums) {
        index.addAll(nums);
        sort();
    }

    @Override
    public synchronized void add(Long num) {
        index.add(num);
        sort();
    }

    @Override
    public synchronized Long peek() {
        return index.get(0);
    }

    @Override
    public synchronized Long poll() {
        Long num = index.get(0);
        index.remove(0);
        return num;
    }

    @Override
    public synchronized boolean contains(Long num) {
        return Collections.binarySearch(index, num) >= 0;
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public synchronized void clear() {
        index.clear();
    }

    private void sort() {
        Collections.sort(index);
    }

    @Override
    public Iterator<Long> iterator() {
        return index.iterator();
    }

    public synchronized void removeAll(Collection<Long> indexes) {
        index.removeAll(indexes);
    }

    @Override
    public synchronized Long lastNumber() {

        if (index.isEmpty()) return null;
        return index.get(index.size() - 1);
    }
}

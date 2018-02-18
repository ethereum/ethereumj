package org.ethereum.vm.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;

/**
 * A null implementation of the {@link Stack} interface. This is good for
 * passing around if stack is not used, or when testing.
 */
public class NullStack implements Stack {

    @Override
    public void setProgramListener(ProgramListener listener) {
        // Intentionally left blank
    }

    @Override
    public synchronized DataWord pop() {
        return new DataWord();
    }

    @Override
    public DataWord push(DataWord item) {
        return new DataWord();
    }

    @Override
    public void swap(int from, int to) {
        // Intentionally left blank
    }

    @Override
    public DataWord peek() {
        return new DataWord();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public DataWord get(int index) {
        return new DataWord();
    }

    @Override
    public DataWord[] toArray() {
        return new DataWord[0];
    }

    @Override
    public boolean add(DataWord e) {
        return false;
    }

    @Override
    public void add(int index, DataWord element) {
        // Intentionally left blank
    }

    @Override
    public boolean addAll(Collection<? extends DataWord> c) {
        // Intentionally left blank
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends DataWord> c) {
        // Intentionally left blank
        return false;
    }

    @Override
    public void clear() {
        // Intentionally left blank
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterator<DataWord> iterator() {
        return new ArrayList<DataWord>(0).iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<DataWord> listIterator() {
        return (ListIterator<DataWord>) iterator();
    }

    @Override
    public ListIterator<DataWord> listIterator(int index) {
        return listIterator();
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public DataWord remove(int index) {
        return new DataWord();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public DataWord set(int index, DataWord element) {
        return element;
    }

    @Override
    public List<DataWord> subList(int fromIndex, int toIndex) {
        return new ArrayList<>();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return a;
    }
}

/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 *      Copyright (C) 2016 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
//Parts of this file are copyrighted by Vedant Kumar <vsk@berkeley.edu>
//https://github.com/aweisberg/quotient-filter/commit/54539e6e287c7f68139733c65ecc4873e2872d54
/*
 * qf.c
 *
 * Copyright (c) 2014 Vedant Kumar <vsk@berkeley.edu>
 */
/*
        Copyright (c) 2014 Vedant Kumar <vsk@berkeley.edu>

        Permission is hereby granted, free of charge, to any person obtaining a
        copy of this software and associated documentation files (the
        "Software"), to deal in the Software without restriction, including
        without limitation the rights to use, copy, modify, merge, publish,
        distribute, sublicense, and/or sell copies of the Software, and to
        permit persons to whom the Software is furnished to do so, subject to
        the following conditions:

        The above copyright notice and this permission notice shall be included
        in all copies or substantial portions of the Software.  THE SOFTWARE IS
        PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
        INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
        FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
        FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
        DEALINGS IN THE SOFTWARE.
*/
package org.ethereum.datasource;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import org.ethereum.util.ByteUtil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.System.arraycopy;
import static java.lang.System.in;
import static java.util.Arrays.copyOfRange;
import static org.ethereum.util.ByteUtil.byteArrayToLong;
import static org.ethereum.util.ByteUtil.longToBytes;

//import net.jpountz.xxhash.XXHashFactory;

public class QuotientFilter implements Iterable<Long> {
//    static final XXHashFactory hashFactory = XXHashFactory.fastestInstance();
    byte QUOTIENT_BITS;
    byte REMAINDER_BITS;
    byte ELEMENT_BITS;
    long INDEX_MASK;
    long REMAINDER_MASK;
    long ELEMENT_MASK;
    long MAX_SIZE;
    long MAX_INSERTIONS;
    int MAX_DUPLICATES = 2;
    long[] table;

    boolean overflowed = false;
    long entries;

    public static QuotientFilter deserialize(byte[] bytes) {
        QuotientFilter ret = new QuotientFilter();
        ret.QUOTIENT_BITS = bytes[0];
        ret.REMAINDER_BITS = bytes[1];
        ret.ELEMENT_BITS = bytes[2];
        ret.INDEX_MASK = byteArrayToLong(copyOfRange(bytes, 3, 11));
        ret.REMAINDER_MASK = byteArrayToLong(copyOfRange(bytes, 11, 19));
        ret.ELEMENT_MASK = byteArrayToLong(copyOfRange(bytes, 19, 27));
        ret.MAX_SIZE = byteArrayToLong(copyOfRange(bytes, 27, 35));
        ret.MAX_INSERTIONS = byteArrayToLong(copyOfRange(bytes, 35, 43));
        ret.overflowed = bytes[43] > 0;
        ret.entries = byteArrayToLong(copyOfRange(bytes, 44, 52));
        ret.table = new long[(bytes.length - 52) / 8];
        for (int i = 0; i < ret.table.length; i++) {
            ret.table[i] = byteArrayToLong(copyOfRange(bytes, 52 + i * 8, 52 + i * 8 + 8));
        }
        return ret;
    }

    public synchronized byte[] serialize() {
        byte[] ret = new byte[1 + 1 + 1 + 8 + 8 + 8 + 8 + 8 + 1 + 8 + table.length * 8];
        ret[0] = QUOTIENT_BITS;
        ret[1] = REMAINDER_BITS;
        ret[2] = ELEMENT_BITS;
        arraycopy(longToBytes(INDEX_MASK), 0, ret, 3, 8);
        arraycopy(longToBytes(REMAINDER_MASK), 0, ret, 11, 8);
        arraycopy(longToBytes(ELEMENT_MASK), 0, ret, 19, 8);
        arraycopy(longToBytes(MAX_SIZE), 0, ret, 27, 8);
        arraycopy(longToBytes(MAX_INSERTIONS), 0, ret, 35, 8);
        ret[43] = (byte) (overflowed ? 1 : 0);
        arraycopy(longToBytes(entries), 0, ret, 44, 8);
        for (int i = 0; i < table.length; i++) {
            arraycopy(longToBytes(table[i]), 0, ret, 52 + i * 8, 8);
        }
        return ret;
    }

    static long LOW_MASK(long n) {
        return (1L << n) - 1L;
    }

    static int TABLE_SIZE(int quotientBits, int remainderBits) {
        long bits = (1 << quotientBits) * (remainderBits + 3);
        long longs = bits / 64;
        return Ints.checkedCast((bits % 64) > 0 ? (longs + 1) : longs);
    }

    static int bitsForNumElementsWithLoadFactor(long numElements) {
        if (numElements == 0) {
            return 1;
        }

        int candidateBits = Long.bitCount(numElements) == 1 ?
                Math.max(1, Long.numberOfTrailingZeros(numElements)) :
                Long.numberOfTrailingZeros(Long.highestOneBit(numElements) << 1L);

        //May need an extra bit due to load factor
        if (((long) (LongMath.pow(2, candidateBits) * 0.75)) < numElements) {
            candidateBits++;
        }

        return candidateBits;
    }

    public static QuotientFilter create(long largestNumberOfElements, long startingElements) {
        Preconditions.checkArgument(largestNumberOfElements >= startingElements);
        Preconditions.checkArgument(startingElements > 0);
        Preconditions.checkArgument(largestNumberOfElements > 0);

        /**
         * The way sizing a quotient filter works is that the quotient bits + remainder bits
         * is the maximum number of elements the filter can store before it runs out of fingerprint bits
         * and can no longer be resized.
         */
        int quotientBits = bitsForNumElementsWithLoadFactor(startingElements);
        int remainderBits = bitsForNumElementsWithLoadFactor(largestNumberOfElements);

        //I am pretty sure that even when completely full you want a non-zero number of remainder bits
        //This also gives some emergency slack where even if you guess largest number of elements wrong it will
        //keep working even if you are very wrong.
        remainderBits += 8;
        remainderBits -= quotientBits;

        return new QuotientFilter(quotientBits, remainderBits);
    }

    private QuotientFilter() {}

    public QuotientFilter(int quotientBits, int remainderBits) {
        Preconditions.checkArgument(quotientBits > 0);
        Preconditions.checkArgument(remainderBits > 0);
        Preconditions.checkArgument(quotientBits + remainderBits <= 64);

        QUOTIENT_BITS = (byte) quotientBits;
        REMAINDER_BITS = (byte) remainderBits;
        ELEMENT_BITS = (byte) (REMAINDER_BITS + 3);
        INDEX_MASK = LOW_MASK(QUOTIENT_BITS);
        REMAINDER_MASK = LOW_MASK(REMAINDER_BITS);
        ELEMENT_MASK = LOW_MASK(ELEMENT_BITS);
        MAX_SIZE = 1 << QUOTIENT_BITS;
        MAX_INSERTIONS = (long) (MAX_SIZE * .75);
        table = new long[TABLE_SIZE(QUOTIENT_BITS, REMAINDER_BITS)];
        entries = 0;
    }

    public QuotientFilter withMaxDuplicates(int maxDuplicates) {
        MAX_DUPLICATES = maxDuplicates;
        return this;
    }

    /* Return QF[idx] in the lower bits. */
    long getElement(long idx) {
        long elt = 0;
        long bitpos = ELEMENT_BITS * idx;
        int tabpos = Ints.checkedCast(bitpos / 64);
        long slotpos = bitpos % 64;
        long spillbits = (slotpos + ELEMENT_BITS) - 64;
        elt = (table[tabpos] >>> slotpos) & ELEMENT_MASK;
        if (spillbits > 0) {
            ++tabpos;
            long x = table[tabpos] & LOW_MASK(spillbits);
            elt |= x << (ELEMENT_BITS - spillbits);
        }
        return elt;
    }

    /* Store the lower bits of elt into QF[idx]. */
    void setElement(long idx, long elt) {
        long bitpos = ELEMENT_BITS * idx;
        int tabpos = Ints.checkedCast(bitpos / 64);
        long slotpos = bitpos % 64;
        long spillbits = (slotpos + ELEMENT_BITS) - 64;
        elt &= ELEMENT_MASK;
        table[tabpos] &= ~(ELEMENT_MASK << slotpos);
        table[tabpos] |= elt << slotpos;
        if (spillbits > 0) {
            ++tabpos;
            table[tabpos] &= ~LOW_MASK(spillbits);
            table[tabpos] |= elt >>> (ELEMENT_BITS - spillbits);
        }
    }

    long incrementIndex(long idx) {
        return (idx + 1) & INDEX_MASK;
    }

    long decrementIndex(long idx) {
        return (idx - 1) & INDEX_MASK;
    }

    static boolean isElementOccupied(long elt) {
        return (elt & 1) != 0;
    }

    static long setElementOccupied(long elt) {
        return elt | 1;
    }

    static long clearElementOccupied(long elt) {
        return elt & ~1;
    }

    static boolean isElementContinuation(long elt) {
        return (elt & 2) != 0;
    }

    static long setElementContinuation(long elt) {
        return elt | 2;
    }

    static long clearElementContinuation(long elt) {
        return elt & ~2;
    }

    static boolean isElementShifted(long elt) {
        return (elt & 4) != 0;
    }

    static long setElementShifted(long elt) {
        return elt | 4;
    }

    static long clearElementShifted(long elt) {
        return elt & ~4;
    }

    static long getElementRemainder(long elt) {
        return elt >>> 3;
    }

    static boolean isElementEmpty(long elt) {
        return (elt & 7) == 0;
    }

    static boolean isElementClusterStart(long elt) {
        return isElementOccupied(elt) & !isElementContinuation(elt) & !isElementShifted(elt);
    }

    static boolean isElementRunStart(long elt) {
        return !isElementContinuation(elt) & (isElementOccupied(elt) | isElementShifted(elt));
    }

    long hashToQuotient(long hash) {
        return (hash >>> REMAINDER_BITS) & INDEX_MASK;
    }

    long hashToRemainder(long hash) {
        return hash & REMAINDER_MASK;
    }

    /* Find the start index of the run for fq (given that the run exists). */
    long findRunIndex(long fq) {
        /* Find the start of the cluster. */
        long b = fq;
        while (isElementShifted(getElement(b))) {
            b = decrementIndex(b);
        }

        /* Find the start of the run for fq. */
        long s = b;
        while (b != fq) {
            do {
                s = incrementIndex(s);
            }
            while (isElementContinuation(getElement(s)));

            do {
                b = incrementIndex(b);
            }
            while (!isElementOccupied(getElement(b)));
        }
        return s;
    }

    /* Insert elt into QF[s], shifting over elements as necessary. */
    void insertInto(long s, long elt) {
        long prev;
        long curr = elt;
        boolean empty;

        do {
            prev = getElement(s);
            empty = isElementEmpty(prev);
            if (!empty) {
                /* Fix up `is_shifted' and `is_occupied'. */
                prev = setElementShifted(prev);
                if (isElementOccupied(prev)) {
                    curr = setElementOccupied(curr);
                    prev = clearElementOccupied(prev);
                }
            }
            setElement(s, curr);
            curr = prev;
            s = incrementIndex(s);
        }
        while (!empty);
    }

    public boolean overflowed() {
        return overflowed;
    }

//    public void insert(byte[] data)
//    {
//        insert(data, 0, data.length);
//    }
//
//    public void insert(byte[] data, int offset, int length) {
//        insert(hashFactory.hash64().hash(data, offset, length, 0));
//    }

    private long hash(byte[] bytes) {
        return (bytes[0] & 0xFFL) << 56 |
                (bytes[1] & 0xFFL) << 48 |
                (bytes[2] & 0xFFL) << 40 |
                (bytes[3] & 0xFFL) << 32 |
                (bytes[4] & 0xFFL) << 24 |
                (bytes[5] & 0xFFL) << 16 |
                (bytes[6] & 0xFFL) << 8 |
                (bytes[7] & 0xFFL);
    }

    public synchronized void insert(byte[] hash) {
        insert(hash(hash));
    }

    public synchronized void insert(long hash) {
        if (maybeContainsXTimes(hash, MAX_DUPLICATES)) return;
        if (entries >= MAX_INSERTIONS | overflowed) {
            //Can't safely process an after overflow
            //Only a buggy program would attempt it
            if (overflowed) {
                throw new OverflowedError();
            }

            //Can still resize if we have enough remainder bits
            if (REMAINDER_BITS > 1) {
                selfResizeDouble();
            } else {
                //The filter can't accept more inserts and is effectively broken
                overflowed = true;
                throw new OverflowedError();
            }
        }

        long fq = hashToQuotient(hash);
        long fr = hashToRemainder(hash);
        long T_fq = getElement(fq);
        long entry = (fr << 3) & ~7;

        /* Special-case filling canonical slots to simplify insert_into(). */
        if (isElementEmpty(T_fq)) {
            setElement(fq, setElementOccupied(entry));
            ++entries;
            return;
        }

        if (!isElementOccupied(T_fq)) {
            setElement(fq, setElementOccupied(T_fq));
        }

        long start = findRunIndex(fq);
        long s = start;

        if (isElementOccupied(T_fq)) {
            /* Move the cursor to the insert position in the fq run. */
            do {
                long rem = getElementRemainder(getElement(s));
                if (rem >= fr) {
                    break;
                }
                s = incrementIndex(s);
            }
            while (isElementContinuation(getElement(s)));

            if (s == start) {
                /* The old start-of-run becomes a continuation. */
                long old_head = getElement(start);
                setElement(start, setElementContinuation(old_head));
            } else {
                /* The new element becomes a continuation. */
                entry = setElementContinuation(entry);
            }
        }

        /* Set the shifted bit if we can't use the canonical slot. */
        if (s != fq) {
            entry = setElementShifted(entry);
        }

        insertInto(s, entry);
        ++entries;
        return;
    }

    private void selfResizeDouble() {
        QuotientFilter qf = resize(MAX_INSERTIONS * 2);
        QUOTIENT_BITS = qf.QUOTIENT_BITS;
        REMAINDER_BITS = qf.REMAINDER_BITS;
        ELEMENT_BITS = qf.ELEMENT_BITS;
        INDEX_MASK = qf.INDEX_MASK;
        REMAINDER_MASK = qf.REMAINDER_MASK;
        ELEMENT_MASK = qf.ELEMENT_MASK;
        MAX_SIZE = qf.MAX_SIZE;
        MAX_INSERTIONS = qf.MAX_INSERTIONS;
        table = qf.table;
        if (qf.entries != entries) {
            throw new AssertionError();
        }
    }

    public boolean maybeContains(byte[] hash) {
        return maybeContains(hash(hash));
    }

    public synchronized boolean maybeContains(long hash) {
        if (overflowed) {
            //Can't check for existence after overflow occurred
            //and things are missing
            throw new OverflowedError();
        }

        long fq = hashToQuotient(hash);
        long fr = hashToRemainder(hash);
        long T_fq = getElement(fq);

        /* If this quotient has no run, give up. */
        if (!isElementOccupied(T_fq)) {
            return false;
        }

        /* Scan the sorted run for the target remainder. */
        long s = findRunIndex(fq);
        do {
            long rem = getElementRemainder(getElement(s));
            if (rem == fr) {
                return true;
            } else if (rem > fr) {
                return false;
            }
            s = incrementIndex(s);
        }
        while (isElementContinuation(getElement(s)));
        return false;
    }

    public synchronized boolean maybeContainsXTimes(long hash, int num) {
        if (overflowed) {
            //Can't check for existence after overflow occurred
            //and things are missing
            throw new OverflowedError();
        }

        long fq = hashToQuotient(hash);
        long fr = hashToRemainder(hash);
        long T_fq = getElement(fq);

        /* If this quotient has no run, give up. */
        if (!isElementOccupied(T_fq)) {
            return false;
        }

        /* Scan the sorted run for the target remainder. */
        long s = findRunIndex(fq);
        int counter = 0;
        do {
            long rem = getElementRemainder(getElement(s));
            if (rem == fr) {
                counter++;
            } else if (rem > fr) {
                break;
            }
            s = incrementIndex(s);
        }
        while (isElementContinuation(getElement(s)));
        return counter >= num;
    }

    /* Remove the entry in QF[s] and slide the rest of the cluster forward. */
    void deleteEntry(long s, long quot) {
        long next;
        long curr = getElement(s);
        long sp = incrementIndex(s);
        long orig = s;

        /*
         * FIXME(vsk): This loop looks ugly. Rewrite.
         */
        while (true) {
            next = getElement(sp);
            boolean curr_occupied = isElementOccupied(curr);

            if (isElementEmpty(next) | isElementClusterStart(next) | sp == orig) {
                setElement(s, 0);
                return;
            } else {
                /* Fix entries which slide into canonical slots. */
                long updated_next = next;
                if (isElementRunStart(next)) {
                    do {
                        quot = incrementIndex(quot);
                    }
                    while (!isElementOccupied(getElement(quot)));

                    if (curr_occupied && quot == s) {
                        updated_next = clearElementShifted(next);
                    }
                }

                setElement(s, curr_occupied ?
                        setElementOccupied(updated_next) :
                        clearElementOccupied(updated_next));
                s = sp;
                sp = incrementIndex(sp);
                curr = next;
            }
        }
    }

    public void remove(byte[] hash) {
        remove(hash(hash));
    }

    public synchronized void remove(long hash) {
        if (maybeContainsXTimes(hash, MAX_DUPLICATES)) return;
        //Can't safely process a remove after overflow
        //Only a buggy program would attempt it
        if (overflowed) {
            throw new OverflowedError();
        }

        long fq = hashToQuotient(hash);
        long fr = hashToRemainder(hash);
        long T_fq = getElement(fq);

        if (!isElementOccupied(T_fq) | entries == 0) {
            //If you remove things that don't exist it's possible you will clobber
            //somethign on a collision, your program is buggy
            throw new NoSuchElementError();
        }

        long start = findRunIndex(fq);
        long s = start;
        long rem;

        /* Find the offending table index (or give up). */
        do {
            rem = getElementRemainder(getElement(s));
            if (rem == fr) {
                break;
            } else if (rem > fr) {
                return;
            }
            s = incrementIndex(s);
        } while (isElementContinuation(getElement(s)));
        if (rem != fr) {
            //If you remove things that don't exist it's possible you will clobber
            //somethign on a collision, your program is buggy
            throw new NoSuchElementError();
        }

        long kill = (s == fq) ? T_fq : getElement(s);
        boolean replace_run_start = isElementRunStart(kill);

        /* If we're deleting the last entry in a run, clear `is_occupied'. */
        if (isElementRunStart(kill)) {
            long next = getElement(incrementIndex(s));
            if (!isElementContinuation(next)) {
                T_fq = clearElementOccupied(T_fq);
                setElement(fq, T_fq);
            }
        }

        deleteEntry(s, fq);

        if (replace_run_start) {
            long next = getElement(s);
            long updated_next = next;
            if (isElementContinuation(next)) {
                /* The new start-of-run is no longer a continuation. */
                updated_next = clearElementContinuation(next);
            }
            if (s == fq && isElementRunStart(updated_next)) {
                /* The new start-of-run is in the canonical slot. */
                updated_next = clearElementShifted(updated_next);
            }
            if (updated_next != next) {
                setElement(s, updated_next);
            }
        }

        --entries;
    }

//    public static QuotientFilter merge(Collection<QuotientFilter> filters) {
//        if (filters.stream().map(filter -> filter.REMAINDER_BITS + filter.QUOTIENT_BITS).distinct().count() != 1) {
//            throw new IllegalArgumentException("All filters must have the same size fingerprint");
//        }
//
//        long totalEntries = filters.stream().collect(Collectors.summingLong(filter -> filter.entries));
//        int requiredQuotientBits = bitsForNumElementsWithLoadFactor(totalEntries);
//        int fingerprintBits = filters.iterator().next().QUOTIENT_BITS + filters.iterator().next().REMAINDER_BITS;
//        int remainderBits = fingerprintBits - requiredQuotientBits;
//
//        if (remainderBits < 1) {
//            throw new IllegalArgumentException("Impossible to merge not enough fingerprint bits");
//        }
//
//        QuotientFilter resultFilter = new QuotientFilter(requiredQuotientBits, remainderBits);
//
//        Iterable<QFIterator> iterators = (Iterable) filters.stream().map(filter -> filter.iterator()).collect(Collectors.toList());
//        Iterator<Long> mergeQFIterator = Iterators.mergeSorted(iterators, Ordering.natural());
//        while (mergeQFIterator.hasNext()) {
//            resultFilter.insert(mergeQFIterator.next());
//        }
//        return resultFilter;
//    }

//    public QuotientFilter merge(QuotientFilter other) {
//        return merge(ImmutableList.of(this, other));
//    }
//
//    public QuotientFilter merge(QuotientFilter... filters) {
//        return merge(Arrays.asList(filters));
//    }

    /*
     * Resizes the filter return a filter with the same contents and space for the minimum specified number
     * of entries. This may allocate a new filter or return the existing filter.
     */
    public QuotientFilter resize(long minimumEntries) {
        if (minimumEntries <= MAX_INSERTIONS) {
            return this;
        }

        int newQuotientBits = bitsForNumElementsWithLoadFactor(minimumEntries);
        int newRemainderBits = QUOTIENT_BITS + REMAINDER_BITS - newQuotientBits;

        if (newRemainderBits < 1) {
            throw new IllegalArgumentException("Not enough fingerprint bits to resize");
        }

        QuotientFilter qf = new QuotientFilter(newQuotientBits, newRemainderBits);
        QFIterator i = new QFIterator();
        while (i.hasNext()) {
            qf.insert(i.nextPrimitive());
        }
        return qf;
    }

    public int getAllocatedBytes() {
        return table.length << 3;
    }

    public void clear() {
        entries = 0;
        Arrays.fill(table, 0L);
    }

    @Override
    public QFIterator iterator() {
        return new QFIterator();
    }

    class QFIterator implements LongIterator {
        long index;
        long quotient;
        long visited;

        QFIterator() {
            /* Mark the iterator as done. */
            visited = entries;

            if (entries == 0) {
                return;
            }

             /* Find the start of a cluster. */
            long start;
            for (start = 0; start < MAX_SIZE; ++start) {
                if (isElementClusterStart(getElement(start))) {
                    break;
                }
            }

            visited = 0;
            index = start;
        }

        @Override
        public boolean hasNext() {
            return entries != visited;
        }

        @Override
        public Long next() {
            return nextPrimitive();
        }

        @Override
        public void remove() {

        }

        public long nextPrimitive() {
            while (hasNext()) {
                long elt = getElement(index);

                /* Keep track of the current run. */
                if (isElementClusterStart(elt)) {
                    quotient = index;
                } else {
                    if (isElementRunStart(elt)) {
                        long quot = quotient;
                        do {
                            quot = incrementIndex(quot);
                        }
                        while (!isElementOccupied(getElement(quot)));
                        quotient = quot;
                    }
                }

                index = incrementIndex(index);

                if (!isElementEmpty(elt)) {
                    long quot = quotient;
                    long rem = getElementRemainder(elt);
                    long hash = (quot << REMAINDER_BITS) | rem;
                    ++visited;
                    return hash;
                }
            }

            throw new NoSuchElementException();
        }
    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//
//        int pad = ((int) (Math.ceil(QUOTIENT_BITS / Math.log(10.0)))) + 1;
//
//        for (int i = 0; i < pad; ++i) {
//            sb.append(' ');
//        }
//
//        sb.append(String.format("| is_shifted | is_continuation | is_occupied | remainder"
//                + " nel=%d\n", entries));
//
//        for (long idx = 0; idx < MAX_SIZE; ++idx) {
//            String idxString = Long.toString(idx);
//            sb.append(idx);
//
//            int fillspace = pad - idxString.length();
//            for (int i = 0; i < fillspace; ++i) {
//                sb.append(' ');
//            }
//            sb.append("| ");
//
//            long elt = getElement(idx);
//            sb.append(String.format("%d          | ", isElementShifted(elt) == false ? 0 : 1));
//            sb.append(String.format("%d               | ", isElementContinuation(elt) == false ? 0 : 1));
//            sb.append(String.format("%d           | ", isElementOccupied(elt) == false ? 0 : 1));
//            sb.append(getElementRemainder(elt)).append(System.lineSeparator());
//        }
//        return sb.toString();
//    }

    public class OverflowedError extends AssertionError {

    }

    public class NoSuchElementError extends AssertionError {

    }

    public interface LongIterator extends Iterator<Long> {


        long nextPrimitive();

        @Override
        Long next();
    }
}

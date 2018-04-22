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
package org.ethereum.util.slicer;

import org.ethereum.core.Encoded;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.ethereum.datasource.MemSizeEstimator.ByteArrayEstimator;

/**
 * Utility method for slicing list of lists by memory usage
 * Lists should be added by {@link #add} function one by one until it returns true
 * Accepts one list in excess of configured max memory, but no more
 * @param <T> type of objects lists are made of, should implement {@link Encoded}
 */
public class EncodedListOfListsSlicer<T extends Encoded> {

    private List<List<T>> entityLists;
    private int maxSize;
    private Consumer<T> purgeConsumer;
    private Function<T, Long> memSizeEstimator = t -> ByteArrayEstimator.estimateSize(t.getEncoded()) + 16;
    private int sizeSum = 80; // ArrayList skeleton

    /**
     * Create slicer with following parameters
     * Objects should be added later with {@link #add(List)}
     * @param maxSize         Maximum size, result list could exceed it by no more than 1 nested list
     * @param purgeConsumer   Purge consumer, executed before object is added to the result
     */
    public EncodedListOfListsSlicer(int maxSize, Consumer<T> purgeConsumer) {
        this.entityLists = new ArrayList<>();
        this.maxSize = maxSize;
        this.purgeConsumer = purgeConsumer;
    }

    /**
     * Create slicer with following parameters
     * Objects should be added later with {@link #add(List)}
     * @param maxSize         Maximum size, result list could exceed it by no more than 1 nested list
     * @param purgeConsumer   Purge consumer, executed before object is added to the result
     * @param memSizeEstimator  input: object (nested one, not list), output: memory size
     */
    public EncodedListOfListsSlicer(int maxSize, Consumer<T> purgeConsumer, Function<T, Long> memSizeEstimator) {
        this.entityLists = new ArrayList<>();
        this.maxSize = maxSize;
        this.purgeConsumer = purgeConsumer;
        this.memSizeEstimator = memSizeEstimator;
    }

    /**
     * Add list to the slicer, entityList is either added completely or not added at all
     * @param entityList new list
     * @return true if you could add more, otherwise false
     */
    public synchronized boolean add(List<T> entityList) {
        if (sizeSum >= maxSize) return false;

        sizeSum += 80; // For the list
        for (T entity : entityList) {
            purgeConsumer.accept(entity);
            sizeSum += memSizeEstimator.apply(entity);
        }
        entityLists.add(entityList);

        return sizeSum < maxSize;
    }

    /**
     * Get the result
     * @return sliced list of lists
     */
    public List<List<T>> getEntityLists() {
        return entityLists;
    }
}

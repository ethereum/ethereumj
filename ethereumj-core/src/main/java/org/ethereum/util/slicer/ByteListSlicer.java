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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.ethereum.datasource.MemSizeEstimator.ByteArrayEstimator;

/**
 * Utility method for slicing list or iterator of byte[] by memory usage
 * Accepts one object in excess of configured max memory, but no more
 */
public class ByteListSlicer {

    private List<byte[]> entities = new ArrayList<>();
    private int sizeSum = 80; // ArrayList skeleton

    /**
     * Create slicer with following parameters
     * @param entities        Objects list, the result will be sliced from it
     * @param maxSize         Maximum size, result list could exceed it by no more than 1 element
     */
    public ByteListSlicer(List<byte[]> entities, int maxSize) {
        for (byte[] entity : entities) {
            if (!(add(entity, maxSize))) break;
        }
    }

    /**
     * Create slicer with following parameters
     * @param entitiesIterator  Objects iterator, it will not be pulled after maxSize is filled
     * @param maxSize           Maximum size, result list could exceed it by no more than 1 element
     */
    public ByteListSlicer(Iterator<byte[]> entitiesIterator, int maxSize) {
        while (entitiesIterator.hasNext()) {
            byte[] entity = entitiesIterator.next();
            if (!(add(entity, maxSize))) break;
        }
    }

    private boolean add(byte[] entity, int maxSize) {
        sizeSum += ByteArrayEstimator.estimateSize(entity);
        this.entities.add(entity);
        return sizeSum < maxSize;
    }

    /**
     * Get the result
     * @return sliced list
     */
    public List<byte[]> getEntities() {
        return entities;
    }
}

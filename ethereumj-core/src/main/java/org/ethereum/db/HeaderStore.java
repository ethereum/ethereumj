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
package org.ethereum.db;

import org.ethereum.core.BlockHeader;
import org.ethereum.datasource.DataSourceArray;
import org.ethereum.datasource.ObjectDataSource;
import org.ethereum.datasource.Serializers;
import org.ethereum.datasource.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * BlockHeaders store
 * Assumes one chain
 * Uses indexes by header hash and block number
 */
public class HeaderStore {

    private static final Logger logger = LoggerFactory.getLogger("general");

    Source<byte[], byte[]> indexDS;
    DataSourceArray<byte[]> index;
    Source<byte[], byte[]> headersDS;
    ObjectDataSource<BlockHeader> headers;

    public HeaderStore() {
    }

    public void init(Source<byte[], byte[]> index, Source<byte[], byte[]> headers) {
        indexDS = index;
        this.index = new DataSourceArray<>(
                new ObjectDataSource<>(index,Serializers.AsIsSerializer, 2048));
        this.headersDS = headers;
        this.headers = new ObjectDataSource<>(headers, Serializers.BlockHeaderSerializer, 512);
    }


    public synchronized BlockHeader getBestHeader() {

        long maxNumber = getMaxNumber();
        if (maxNumber < 0) return null;

        return getHeaderByNumber(maxNumber);
    }

    public synchronized void flush() {
        headers.flush();
        index.flush();
        headersDS.flush();
        indexDS.flush();
    }

    public synchronized void saveHeader(BlockHeader header) {
        index.set((int) header.getNumber(), header.getHash());
        headers.put(header.getHash(), header);
    }

    public synchronized BlockHeader getHeaderByNumber(long number) {
        if (number < 0 || number >= index.size()) {
            return null;
        }

        byte[] hash = index.get((int) number);
        if (hash == null) {
            return null;
        }

        return headers.get(hash);
    }

    public synchronized int size() {
        return index.size();
    }

    public synchronized BlockHeader getHeaderByHash(byte[] hash) {
        return headers.get(hash);
    }

    public synchronized long getMaxNumber(){
        if (index.size() > 0) {
            return (long) index.size() - 1;
        } else {
            return -1;
        }
    }
}

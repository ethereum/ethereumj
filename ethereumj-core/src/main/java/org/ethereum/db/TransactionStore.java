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

import org.apache.commons.collections4.map.LRUMap;
import org.ethereum.datasource.*;
import org.ethereum.core.TransactionInfo;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Storage (tx hash) => List of (block idx, tx idx, TransactionReceipt)
 *
 * Since a transaction could be included into blocks from different forks and
 * have different receipts the class stores all of them (the same manner fork blocks are stored)
 *
 * NOTE: the TransactionInfo instances returned contains TransactionReceipt which
 * has no initialized Transaction object. If needed use BlockStore to retrieve and setup
 * Transaction instance
 *
 * Created by Anton Nashatyrev on 07.04.2016.
 */
@Component
public class TransactionStore extends ObjectDataSource<List<TransactionInfo>> {
    private static final Logger logger = LoggerFactory.getLogger("db");

    private final LRUMap<ByteArrayWrapper, Object> lastSavedTxHash = new LRUMap<>(5000);
    private final Object object = new Object();

    private final static Serializer<List<TransactionInfo>, byte[]> serializer =
            new Serializer<List<TransactionInfo>, byte[]>() {
        @Override
        public byte[] serialize(List<TransactionInfo> object) {
            byte[][] txsRlp = new byte[object.size()][];
            for (int i = 0; i < txsRlp.length; i++) {
                txsRlp[i] = object.get(i).getEncoded();
            }
            return RLP.encodeList(txsRlp);
        }

        @Override
        public List<TransactionInfo> deserialize(byte[] stream) {
            try {
                if (stream == null) return null;
                RLPList params = RLP.decode2(stream);
                RLPList infoList = (RLPList) params.get(0);
                List<TransactionInfo> ret = new ArrayList<>();
                for (int i = 0; i < infoList.size(); i++) {
                    ret.add(new TransactionInfo(infoList.get(i).getRLPData()));
                }
                return ret;
            } catch (Exception e) {
                // fallback to previous DB version
                return Collections.singletonList(new TransactionInfo(stream));
            }
        }
    };

    /**
     * Adds TransactionInfo to the store.
     * If entries for this transaction already exist the method adds new entry to the list
     * if no entry for the same block exists
     * @return true if TransactionInfo was added, false if already exist
     */
    public boolean put(TransactionInfo tx) {
        byte[] txHash = tx.getReceipt().getTransaction().getHash();

        List<TransactionInfo> existingInfos = null;
        synchronized (lastSavedTxHash) {
            if (lastSavedTxHash.put(new ByteArrayWrapper(txHash), object) != null || !lastSavedTxHash.isFull()) {
                existingInfos = get(txHash);
            }
        }
        // else it is highly unlikely that the transaction was included into another block
        // earlier than 5000 transactions before with regard to regular block import process

        if (existingInfos == null) {
            existingInfos = new ArrayList<>();
        } else {
            for (TransactionInfo info : existingInfos) {
                if (FastByteComparisons.equal(info.getBlockHash(), tx.getBlockHash())) {
                    return false;
                }
            }
        }
        existingInfos.add(tx);
        put(txHash, existingInfos);

        return true;
    }

    public TransactionInfo get(byte[] txHash, byte[] blockHash) {
        List<TransactionInfo> existingInfos = get(txHash);
        for (TransactionInfo info : existingInfos) {
            if (FastByteComparisons.equal(info.getBlockHash(), blockHash)) {
                return info;
            }
        }
        return null;
    }

    public TransactionStore(Source<byte[], byte[]> src) {
        super(src, serializer, 256);
    }

    @PreDestroy
    public void close() {
//        try {
//            logger.info("Closing TransactionStore...");
//            super.close();
//        } catch (Exception e) {
//            logger.warn("Problems closing TransactionStore", e);
//        }
    }
}

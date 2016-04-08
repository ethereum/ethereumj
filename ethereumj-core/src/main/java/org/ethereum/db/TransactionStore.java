package org.ethereum.db;

import org.ethereum.datasource.*;
import org.ethereum.db.TransactionInfo;
import org.springframework.stereotype.Component;

/**
 * Storage (tx hash) => (block idx, tx idx, TransactionReceipt)
 *
 * Created by Anton Nashatyrev on 07.04.2016.
 */
@Component
public class TransactionStore extends ObjectDataSource<TransactionInfo> {
    private final static Serializer<TransactionInfo, byte[]> serializer =
            new Serializer<TransactionInfo, byte[]>() {
        @Override
        public byte[] serialize(TransactionInfo object) {
            return object.getEncoded();
        }

        @Override
        public TransactionInfo deserialize(byte[] stream) {
            return new TransactionInfo(stream);
        }
    };

    public TransactionStore(KeyValueDataSource src) {
        super(src, serializer);
        withCacheSize(256);
        withCacheOnWrite(true);
    }

    @Override
    public void flush() {
        if (getSrc() instanceof Flushable) {
            ((Flushable) getSrc()).flush();
        }
    }
}

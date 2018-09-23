package org.ethereum.kafka.db;

import org.ethereum.datasource.AbstractChainedSource;
import org.ethereum.datasource.Source;
import org.ethereum.kafka.Kafka;
import org.ethereum.util.ByteUtil;

public class KafkaListeningDataSource extends AbstractChainedSource<byte[], byte[], byte[], byte[]> {

    private final Kafka kafka;

    public KafkaListeningDataSource(Kafka kafka, Source<byte[], byte[]> delegate) {
        super(delegate);
        this.kafka = kafka;
    }

    @Override
    public void put(byte[] key, byte[] val) {
        kafka.sendSync(Kafka.Producer.ACCOUNT_STATE, ByteUtil.toHexString(key), val);
        getSource().put(key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        return getSource().get(key);
    }

    @Override
    public void delete(byte[] key) {
        kafka.sendSync(Kafka.Producer.ACCOUNT_STATE, ByteUtil.toHexString(key), null);
        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }
}

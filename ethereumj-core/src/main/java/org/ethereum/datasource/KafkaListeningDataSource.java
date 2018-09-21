package org.ethereum.datasource;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.ethereum.util.ByteUtil;

public class KafkaListeningDataSource extends AbstractChainedSource<byte[], byte[], byte[], byte[]> {

    private final KafkaProducer<String, byte[]> kafkaProducer;

    public KafkaListeningDataSource(Source<byte[], byte[]> delegate, KafkaProducer<String, byte[]> kafkaProducer) {
        super(delegate);
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void put(byte[] key, byte[] val) {

        try {
            kafkaProducer.send(new ProducerRecord<>("state", ByteUtil.toHexString(key), val)).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getSource().put(key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        return getSource().get(key);
    }

    @Override
    public void delete(byte[] key) {

        try {
            // tombstone
            kafkaProducer.send(new ProducerRecord<>("state", ByteUtil.toHexString(key), null)).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }
}

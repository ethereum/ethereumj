package org.ethereum.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special optimization when the majority of get requests to the slower underlying source
 * are targeted to missing entries. The BloomFilter handles most of these requests.
 *
 * WARN: can be used only when the backing source is initially empty or
 * if the backing source contains any values the BloomFilter instance passed should
 * be filled according to these values
 *
 * Created by Anton Nashatyrev on 16.01.2017.
 */
public class BloomedSource<Value> extends AbstractChainedSource<byte[], Value, byte[], Value> {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    BloomFilter bloom;
    int hits = 0;
    int misses = 0;
    int falseMisses = 0;

    public BloomedSource(Source<byte[], Value> source) {
        super(source);
//
//        new Thread() {
//            @Override
//            public void run() {
//                while(true) {
//                    synchronized (BloomedSource.this) {
//                        logger.debug("BloomedSource: hits: " + hits + ", misses: " + misses + ", false: " + falseMisses);
//                        hits = misses = falseMisses = 0;
//                    }
//
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {}
//                }
//            }
//        }.start();
    }

    public void startBlooming(BloomFilter filter) {
        bloom = filter;
    }

    public void stopBlooming() {
        bloom = null;
    }

    @Override
    public synchronized void put(byte[] key, Value val) {
        if (bloom != null) {
            bloom.add(key);
        }
        getSource().put(key, val);
    }

    @Override
    public synchronized Value get(byte[] key) {
        if (bloom == null) return getSource().get(key);

        if (!bloom.contains(key)) {
            hits++;
            return null;
        } else {
            Value ret = getSource().get(key);
            if (ret == null) falseMisses++; else misses++;
            return ret;
        }
    }

    @Override
    public void delete(byte[] key) {
        getSource().delete(key);
    }

    @Override
    protected boolean flushImpl() {
        return false;
    }
}

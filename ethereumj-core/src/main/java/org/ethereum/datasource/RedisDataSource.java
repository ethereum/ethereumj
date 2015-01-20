package org.ethereum.datasource;

import org.ethereum.db.Database;
import org.ethereum.db.DatabaseImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 18/01/2015 21:48
 */

public class RedisDataSource implements KeyValueDataSource{

    String name;
    int index;
    
    Jedis jedis;
    
    
    @Override
    public void init() {
        
        if (name == null) throw new NullPointerException("no name set to the db");
        
        this.jedis = RedisPool.getResource();
        if (jedis == null) this.jedis = new Jedis("localhost");
        
        if (CONFIG.databaseReset())
            this.jedis.flushAll();
    }

    @Override
    public void setName(String name) {
        this.name = name;
        index = nameToIndex(name);
    }
    
    @Override
    public byte[] get(byte[] key) {
        jedis.select(index);
        return jedis.get(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        jedis.select(index);
        jedis.set(key, value);
    }

    @Override
    public void delete(byte[] key) {
        jedis.select(index);
        jedis.del(key);
    }

    @Override
    public Set<byte[]> keys() {
        return jedis.keys("*".getBytes());
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        jedis.select(index);
        Pipeline pipeline = jedis.pipelined();

        Iterator<Map.Entry<byte[], byte[]>> iterator = rows.entrySet().iterator();
        while(iterator.hasNext()){

            Map.Entry<byte[], byte[]> row = iterator.next();
            byte[] key = row.getKey();
            byte[] val = row.getValue();
            pipeline.set(key, val);
        }
        pipeline.sync();
    }

    @Override
    public void close() {
        jedis.close();
    }

    private static Integer nameToIndex(String name) {
        Integer index = DBNameScheme.get(name);
        if (index == null) {
            index = indexCounter.getAndIncrement();
            DBNameScheme.put(name, index);
            indexCounter.intValue();
        }
        return index;
    }
    
    private static Map<String, Integer> DBNameScheme = new HashMap<>();
    private static AtomicInteger indexCounter = new AtomicInteger(1);
}

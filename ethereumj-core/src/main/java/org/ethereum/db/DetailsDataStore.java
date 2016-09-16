package org.ethereum.db;

import org.ethereum.config.CommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.ethereum.util.ByteUtil.wrap;

@Component
public class DetailsDataStore {

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    private static final Logger gLogger = LoggerFactory.getLogger("general");

    private DatabaseImpl db = null;
    private Map<ByteArrayWrapper, ContractDetails> cache = new ConcurrentHashMap<>();
    private Set<ByteArrayWrapper> removes = new HashSet<>();

    public DetailsDataStore() {
    }

    public void setDB(DatabaseImpl db) {
        this.db = db;
    }

    public synchronized ContractDetails get(byte[] key) {

        ByteArrayWrapper wrappedKey = wrap(key);
        ContractDetails details = cache.get(wrappedKey);

        if (details == null) {

            if (removes.contains(wrappedKey)) return null;
            byte[] data = db.get(key);
            if (data == null) return null;

            ContractDetailsImpl detailsImpl = commonConfig.contractDetailsImpl();
            detailsImpl.setDataSource(db.getDb());
            detailsImpl.decode(data);
            details = detailsImpl;

            cache.put(wrappedKey, details);

            float out = ((float) data.length) / 1048576;
            if (out > 10) {
                String sizeFmt = format("%02.2f", out);
                gLogger.debug("loaded: key: " + Hex.toHexString(key) + " size: " + sizeFmt + "MB");
            }
        }


        return details;
    }

    public synchronized void update(byte[] key, ContractDetails contractDetails) {
        contractDetails.setAddress(key);

        ByteArrayWrapper wrappedKey = wrap(key);
        cache.put(wrappedKey, contractDetails);
        removes.remove(wrappedKey);
    }

    public synchronized void remove(byte[] key) {
        ByteArrayWrapper wrappedKey = wrap(key);
        cache.remove(wrappedKey);
        removes.add(wrappedKey);
    }

    public synchronized void flush() {
        long keys = cache.size();

        long start = System.nanoTime();
        long totalSize = flushInternal();
        long finish = System.nanoTime();

        float flushSize = (float) totalSize / 1_048_576;
        float flushTime = (float) (finish - start) / 1_000_000;
        gLogger.info(format("Flush details in: %02.2f ms, %d keys, %02.2fMB", flushTime, keys, flushSize));
    }

    private long flushInternal() {
        long totalSize = 0;

        Map<byte[], byte[]> batch = new HashMap<>();
        for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : cache.entrySet()) {
            ContractDetails details = entry.getValue();
            details.syncStorage();

            byte[] key = entry.getKey().getData();
            byte[] value = details.getEncoded();

            batch.put(key, value);
            totalSize += value.length;
        }

        db.getDb().updateBatch(batch);

        for (ByteArrayWrapper key : removes) {
            db.delete(key.getData());
        }

        cache.clear();
        removes.clear();

        return totalSize;
    }


    public synchronized Set<ByteArrayWrapper> keys() {
        Set<ByteArrayWrapper> keys = new HashSet<>();
        keys.addAll(cache.keySet());
        keys.addAll(db.dumpKeys());

        return keys;
    }


    private void temporarySave(String addr, byte[] data){
        try {
            FileOutputStream fos = new FileOutputStream(addr);
            fos.write(data);
            fos.close();
            System.out.println("drafted: " + addr);
        } catch (IOException e) {e.printStackTrace();}
    }

    @PreDestroy
    public synchronized void close() {
        try {
            gLogger.info("Closing DetailsDataStore");
            db.close();
        } catch (Exception e) {
            gLogger.warn("Problems closing DetailsDataStore", e);
        }
    }
}

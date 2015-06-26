package org.ethereum.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ethereum.util.ByteUtil.wrap;

public class DetailsDataStore {

    private static final Logger gLogger = LoggerFactory.getLogger("general");

    private DatabaseImpl db = null;
    private HashMap<ByteArrayWrapper, ContractDetails> cache = new HashMap<>();
    private Set<ByteArrayWrapper> removes = new HashSet<>();

    public void setDB(DatabaseImpl db){
        this.db = db;
    }

    public ContractDetails get(byte[] key){

        ContractDetails details = cache.get(wrap(key));

        if (details == null){
            byte[] data = db.get(key);
            if (data == null) return null;

            details = new ContractDetailsImpl(data);
            cache.put( wrap(key), details);

            float out = ((float)data.length) / 1048576;
            if (out > 10) {
                String sizeFmt = String.format("%02.2f", out);
                System.out.println("loaded: key: " + Hex.toHexString(key) + " size: " + sizeFmt + "MB");
            }
        }

        return details;
    }

    public void update(byte[] key, ContractDetails contractDetails){
        cache.put(wrap(key), contractDetails);

        if (removes.contains(wrap(key)))
            removes.remove(wrap(key));
    }

    public void remove(byte[] key){
        cache.remove(wrap(key));
        removes.add(wrap(key));
    }

    public void flush(){

        long t = System.nanoTime();

        Map<byte[], byte[]> batch = new HashMap<>();
        long totalSize = 0;
        for (ByteArrayWrapper key : cache.keySet()){
            ContractDetails contractDetails = cache.get(key);
            byte[] value = contractDetails.getEncoded();
            db.put(key.getData(), value);
            batch.put(key.getData(), value);
            totalSize += value.length;
        }

        db.getDb().updateBatch(batch);

        for (ByteArrayWrapper key : removes){
            db.delete(key.getData());
        }

        long keys = cache.size();

        byte[] aKey = Hex.decode("b61662398570293e4f0d25525e2b3002b7fe0836");
        ContractDetails aDetails = cache.get(wrap(aKey));

        cache.clear();
        removes.clear();

        if (aDetails != null) cache.put(wrap(aKey), aDetails);

        long t_ = System.nanoTime();
        String sizeFmt = String.format("%02.2f", ((float)totalSize) / 1048576);
        gLogger.info("Flush details in: {} ms, {} keys, {}MB",
                ((float)(t_ - t) / 1_000_000), keys, sizeFmt);
    }


    public Set<ByteArrayWrapper> keys(){

        Set<ByteArrayWrapper> keys = new HashSet<>();
        keys.addAll(cache.keySet());
        keys.addAll(db.dumpKeys());
        return keys;
    }
}

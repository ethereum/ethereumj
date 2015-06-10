package org.ethereum.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
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

            if ( removes.contains(wrap(key)))  return null;

            byte[] data = db.get(key);
            if (data == null) return null;

            details = new ContractDetailsImpl(data);
            cache.put( wrap(key), details);
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

        for (ByteArrayWrapper key : cache.keySet()){
            ContractDetails contractDetails = cache.get(key);
            byte[] value = contractDetails.getEncoded();
            db.put(key.getData(), value);
        }

        for (ByteArrayWrapper key : removes){
            db.delete(key.getData());
        }

        long keys = cache.size();

        cache.clear();
        removes.clear();

        long t_ = System.nanoTime();
        gLogger.info("Flush details in: {} ms, {} keys", ((float)(t_ - t) / 1_000_000), keys);
    }


    public Set<ByteArrayWrapper> keys(){
        
        Set<ByteArrayWrapper> keys = new HashSet<>();
        keys.addAll(cache.keySet());
        keys.addAll(db.dumpKeys());
        return keys;
    }
}

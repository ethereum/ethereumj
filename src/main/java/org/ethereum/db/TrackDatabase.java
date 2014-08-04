package org.ethereum.db;

import java.util.HashMap;
import java.util.Map;

import org.ethereum.util.LRUMap;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 14:09
 */
public class TrackDatabase implements Database {

//	private static final int MAX_ENTRIES = 1000; // Should contain most commonly hashed values 
//	private static LRUMap<ByteArrayWrapper, byte[]> valueCache = new LRUMap<>(0, MAX_ENTRIES);
	
    private Database db;

    private boolean trackingChanges;
    private Map<ByteArrayWrapper, byte[]> changes;
    private Map<ByteArrayWrapper, byte[]> deletes;

    public TrackDatabase(Database db) {
        this.db = db;
    }

    public void startTrack() {
        changes = new HashMap<>();
        deletes = new HashMap<>();
        trackingChanges = true;
    }

    public void commitTrack() {
        for(ByteArrayWrapper key : changes.keySet()) {
            db.put(key.getData(), changes.get(key));
        }
        changes = null;
        trackingChanges = false;
    }

    public void rollbackTrack() {
        changes = new HashMap<>();
        deletes = new HashMap<>();
        changes = null;
        trackingChanges = false;
    }

    public void put(byte[] key, byte[] value) {
//    	valueCache.put(wKey, value);
        if (trackingChanges) {
        	ByteArrayWrapper wKey = new ByteArrayWrapper(key);
			changes.put(wKey, value);
        } else {
            db.put(key, value);
        }
    }

    public byte[] get(byte[] key) {
    	if(trackingChanges) {
    		ByteArrayWrapper wKey = new ByteArrayWrapper(key);
            if (deletes.get(wKey) != null) return null;
            if (changes.get(wKey) != null) return changes.get(wKey);
        }
//    	byte[] result = valueCache.get(wKey);
//        if(result != null)
//        	return result;
       	return db.get(key);
    }

    /** Delete object (key) from db **/
    public void delete(byte[] key) {
        if (trackingChanges) {
            ByteArrayWrapper wKey = new ByteArrayWrapper(key);
            deletes.put(wKey, null);
        } else {
            db.delete(key);
        }
    }

    @Override
    public void close(){
        db.close();
    }
}

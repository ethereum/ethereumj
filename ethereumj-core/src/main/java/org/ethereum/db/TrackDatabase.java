package org.ethereum.db;

import java.util.HashMap;
import java.util.Map;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 11/06/2014 14:09
 */
public class TrackDatabase implements Database {

    private Database db;

    private boolean trackingChanges;
    private Map<ByteArrayWrapper, byte[]> changes;
    private Map<ByteArrayWrapper, byte[]> deletes;

    public TrackDatabase(Database db) {
        this.db = db;
    }

    public void startTrack() {
        changes = new HashMap<ByteArrayWrapper, byte[]>();
        deletes = new HashMap<ByteArrayWrapper, byte[]>();
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
        changes = new HashMap<ByteArrayWrapper, byte[]>();
        deletes = new HashMap<ByteArrayWrapper, byte[]>();
        changes = null;
        trackingChanges = false;
    }

    public void put(byte[] key, byte[] value) {
        if (trackingChanges) {
            changes.put(  new ByteArrayWrapper(key)  , value);
        } else {
            db.put(key, value);
        }
    }

    public byte[] get(byte[] key) {
        if(trackingChanges) {
            ByteArrayWrapper wKey = new ByteArrayWrapper(key);
            if (deletes.get(wKey) != null) return null;
            if (changes.get(wKey) != null) return changes.get(wKey);
            return db.get(key);
        }
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
}

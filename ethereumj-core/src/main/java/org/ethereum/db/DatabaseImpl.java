package org.ethereum.db;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ethereum.config.SystemProperties;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

/**
 *  Generic interface for Ethereum database
 *	
 *	LevelDB key/value pair DB implementation will be used.
 *  Choice must be made between:
 *  	Pure Java: https://github.com/dain/leveldb
 *  	JNI binding: https://github.com/fusesource/leveldbjni
 */
public class DatabaseImpl implements Database {
	
	private static final Logger logger = LoggerFactory.getLogger("db");
	private DB db;
	private String name;
    
	public DatabaseImpl(String name) {
    	// Initialize Database
        this.name = name;
		Options options = new Options();
		options.createIfMissing(true);
		options.compressionType(CompressionType.NONE);
		try {
			logger.debug("Opening database");
            File dbLocation = new File(System.getProperty("user.dir") + "/" +
                                       SystemProperties.CONFIG.databaseDir() + "/");
            File fileLocation = new File(dbLocation, name);

			if(SystemProperties.CONFIG.databaseReset()) {
				destroyDB(fileLocation);
			}

			logger.debug("Initializing new or existing database: '{}'", name);
			db = factory.open(fileLocation, options);
//			logger.debug("Showing database stats");
//			String stats = DATABASE.getProperty("leveldb.stats");
//			logger.debug(stats);

            if (logger.isTraceEnabled()){

                logger.trace("Dump for: {}", fileLocation.toString());
                DBIterator iter =  db.iterator();

                while(iter.hasNext()){
                    byte[] key   = iter.peekNext().getKey();
                    byte[] value = iter.peekNext().getValue();

                    logger.trace("key={}, value={}", Hex.toHexString(key), Hex.toHexString(value));
                    iter.next();
                }
            }
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
			throw new RuntimeException("Can't initialize database");
		}		
	}
	
	public void destroyDB(File fileLocation) {
		logger.debug("Destroying existing database");
		Options options = new Options();
		try {
			factory.destroy(fileLocation, options);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public byte[] get(byte[] key) {
		return db.get(key);
	}
	
	@Override
	public void put(byte[] key, byte[] value) {

        if(logger.isDebugEnabled())
            logger.debug("put: key: [{}], value: [{}]",
                    Hex.toHexString(key),
                    Hex.toHexString(value));
		db.put(key, value);
	}
	
	@Override
	public void delete(byte[] key) {
        if(logger.isDebugEnabled())
            logger.debug("delete: key: [{}]");

		db.delete(key);
	}
	
	public DBIterator iterator() {
		return db.iterator();
	}
	
	public DB getDb() {
		return this.db;
	}

    @Override
    public void close() {
        try {
            logger.info("Close db: {}", name);
            db.close();
        } catch (IOException e) {
            logger.error("Failed to find the db file on the close: {} ", name);
        }
    }

	public List<ByteArrayWrapper> dumpKeys() {
		DBIterator iterator = getDb().iterator();
		ArrayList<ByteArrayWrapper> keys = new ArrayList<>();

		while (iterator.hasNext()) {
			ByteArrayWrapper key = new ByteArrayWrapper(iterator.next().getKey());
			keys.add(key);
		}
		Collections.sort((List<ByteArrayWrapper>) keys);
		return keys;
	}
}
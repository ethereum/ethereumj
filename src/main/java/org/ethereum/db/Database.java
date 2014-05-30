package org.ethereum.db;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.net.message.StaticMessages;
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
public class Database {
	
	private static Logger logger = LoggerFactory.getLogger(Database.class);
	private DB db;
    private String name;

	
	public Database(String name) {
    	// Initialize Database
        this.name = name;
		Options options = new Options();
		options.createIfMissing(true);
		try {
			logger.debug("Opening database");
			if(SystemProperties.CONFIG.databaseReset()) {
				logger.debug("Destroying '" + name + "' DB on startup ENABLED");
				destroyDB(name);
			}
			logger.debug("Initializing new or existing DB: '" + name + "'");
			options.createIfMissing(true);
			db = factory.open(new File(name), options);

			printDB();
//			logger.debug("Showing database stats");
//			String stats = DATABASE.getProperty("leveldb.stats");
//			logger.debug(stats);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
			throw new RuntimeException("Can't initialize database");
		}		
	}
	
	public void destroyDB(String name) {
		logger.debug("Destroying existing DB");
		Options options = new Options();
		try {
			factory.destroy(new File(name), options);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void printDB() {
		DBIterator iterator = db.iterator();
		try {
			Map<Long, Block> blocks = new HashMap<Long, Block>();
			int count = 0;
			if (!iterator.hasNext()) {
				logger.info("DB is empty");
			} else {
				logger.info("Displaying blocks stored in DB sorted on key");
			}
			for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
				byte[] key = iterator.peekNext().getKey();
				byte[] value = iterator.peekNext().getValue();
				Block block = new Block(value);
				blocks.put(new Long(block.getNumber()), block);
				logger.info("Block: " + count + " Key: " + Hex.toHexString(key) + " ---> " + block.toFlatString());
				count++;
			}
		} finally {
			// Make sure you close the iterator to avoid resource leaks.
			try {
				iterator.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Insert object(value) (key = sha3(value)) */
	public void put(byte[] key, byte[] value) {
		db.put(key, value);
	}
	
	/** Get object (key) -> value */
	public byte[] get(byte[] key) {
		return db.get(key);
	}
	
	/** Delete object (key) from db **/
	public void delete(byte[] key) {
		delete(key);
	}
	
	public DBIterator iterator() {
		return db.iterator();
	}
	
	public DB getDb() {
		return this.db;
	}

    public void close(){
        try {
            logger.info("Release DB: {}", name);
            db.close();
        } catch (IOException e) {
            logger.error("failed to find the db file on the close: {} ", name);
        }
    }

}
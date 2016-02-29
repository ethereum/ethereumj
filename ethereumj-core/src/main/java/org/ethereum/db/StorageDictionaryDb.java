package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.datasource.*;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.VMUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;

/**
 * DB managing the Layout => Contract => StorageDictionary mapping
 *
 * Created by Anton Nashatyrev on 10.09.2015.
 */
//@Component
public class StorageDictionaryDb {
    private static final Logger logger = LoggerFactory.getLogger("repository");

    @Autowired
    ApplicationContext appCtx;

    public enum Layout {
        Solidity("solidity"),
        Serpent("serpent");

        private String dbName;

        Layout(String dbName) {
            this.dbName = dbName;
        }

        public String getDbName() {
            return dbName;
        }
    }

    public static StorageDictionaryDb INST = new StorageDictionaryDb().init();

    private DB storagekeysDb;
    Map<Layout, HTreeMap<ByteArrayWrapper, StorageDictionary>> layoutDbs = new HashMap<>();

    KeyValueDataSource db;
    CachingDataSource cachingDb;

    private StorageDictionaryDb() {
    }

//    @PostConstruct
    StorageDictionaryDb init() {
//        db = appCtx.getBean(LevelDbDataSource.class, "storageDict");
        db = new LevelDbDataSource("storageDict");
//        db = new HashMapDB();
        db.init();
        cachingDb = new CachingDataSource(db);
        return this;
    }

    public void flush() {
        cachingDb.flush();
    }

    public void close() {
        flush();
        db.close();
    }

    public StorageDictionary get(Layout layout, byte[] contractAddress) {
        StorageDictionary storageDictionary = getOrCreate(layout, contractAddress);
        return storageDictionary.isExist() ? storageDictionary : null;
    }

    public StorageDictionary getOrCreate(Layout layout, byte[] contractAddress) {
        byte[] key = ByteUtil.xorAlignRight(SHA3Helper.sha3(layout.getDbName().getBytes()), contractAddress);
        XorDataSource dataSource = new XorDataSource(cachingDb, key);
        return new StorageDictionary(dataSource);
    }
}

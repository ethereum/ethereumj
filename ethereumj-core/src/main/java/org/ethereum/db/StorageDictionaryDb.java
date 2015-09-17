package org.ethereum.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.config.SystemProperties;
import org.ethereum.vm.VMUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import javax.annotation.PostConstruct;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private static final Serializer<StorageDictionary> SERIALIZER = new Serializer<StorageDictionary>() {
        @Override
        public void serialize(DataOutput out, StorageDictionary value) throws IOException {
            byte[] bytes = VMUtils.compress(value.serializeToJson());
            BYTE_ARRAY.serialize(out, bytes);
        }

        @Override
        public StorageDictionary deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            String json = new String(VMUtils.decompress(bytes), StandardCharsets.UTF_8);
            return StorageDictionary.deserializeFromJson(json);
        }
    };

    private static final Serializer<ByteArrayWrapper> KEY_SERIALIZER = new Serializer<ByteArrayWrapper>() {
        @Override
        public void serialize(DataOutput out, ByteArrayWrapper value) throws IOException {
            BYTE_ARRAY.serialize(out, value.getData());
        }

        @Override
        public ByteArrayWrapper deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            return new ByteArrayWrapper(bytes);
        }
    };

    public static StorageDictionaryDb INST = new StorageDictionaryDb().init();

    private DB storagekeysDb;
    Map<Layout, HTreeMap<ByteArrayWrapper, StorageDictionary>> layoutDbs = new HashMap<>();

    private StorageDictionaryDb() {
    }

    @PostConstruct
    StorageDictionaryDb init() {
        File dbFile = new File(getProperty("user.dir") + "/" + SystemProperties.CONFIG.databaseDir() + "/metadata/storagedict");
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        storagekeysDb = DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown()
                .make();

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                storagekeysDb.commit();
//            }
//        }, 1000, 1000);

        return this;
    }

    HTreeMap<ByteArrayWrapper, StorageDictionary> getLayoutTable(Layout layout) {
        HTreeMap<ByteArrayWrapper, StorageDictionary> ret = layoutDbs.get(layout);
        if (ret == null) {
            ret = storagekeysDb.hashMapCreate(layout.getDbName())
                    .keySerializer(KEY_SERIALIZER)
                    .valueSerializer(SERIALIZER)
                    .makeOrGet();
            layoutDbs.put(layout, ret);
        }
        return ret;
    }

    public void close() {
        storagekeysDb.close();
        layoutDbs.clear();
    }

    public StorageDictionary get(Layout layout, byte[] contractAddress) {
        return getLayoutTable(layout).get(new ByteArrayWrapper(contractAddress));
    }

    public StorageDictionary getOrCreate(Layout layout, byte[] contractAddress) {
        StorageDictionary ret = get(layout, contractAddress);
        if (ret == null) {
            ret = new StorageDictionary();
            put(layout, contractAddress, ret);
        }
        return ret;
    }

    public void put(Layout layout, byte[] contractAddress, StorageDictionary keys) {
        logger.debug("Update storage dictionary for contract " + Hex.toHexString(contractAddress));
        if (!keys.isValid()) {
            getLayoutTable(layout).put(new ByteArrayWrapper(contractAddress), keys);
            File f = new File("json");
            f.mkdirs();
            f = new File(f, Hex.toHexString(contractAddress) + ".json");
            ObjectMapper om = new ObjectMapper();
            try {
                om.writerWithDefaultPrettyPrinter().writeValue(f, keys.root);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            logger.debug("Storage dictionary changed. Committing to DB: " + Hex.toHexString(contractAddress));

            storagekeysDb.commit();

            keys.validate();
        }
    }
}

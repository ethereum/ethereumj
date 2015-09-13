package org.ethereum.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.vm.VMUtils;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DB managing the Contract => StorageDictionary mapping
 *
 * Created by Anton Nashatyrev on 10.09.2015.
 */
//@Component
public class StorageDictionaryDb {
    private static final Logger logger = LoggerFactory.getLogger("repository");
    private static final Serializer<StorageDictionary> SERIALIZER = new Serializer<StorageDictionary>() {
        @Override
        public void serialize(DataOutput out, StorageDictionary value) throws IOException {
            byte[] bytes = VMUtils.compress(value.serializeToJson());
            BYTE_ARRAY.serialize(out, bytes);
        }

        @Override
        public StorageDictionary deserialize(DataInput in, int available) throws IOException {
            byte[] bytes = BYTE_ARRAY.deserialize(in, available);
            String json = VMUtils.decompress(bytes);
            return StorageDictionary.deserializeFromJson(json);
        }
    };

    public static StorageDictionaryDb INST = new StorageDictionaryDb().init();

    //    @Autowired
    MapDBFactory mapDBFactory = new MapDBFactoryImpl();

    private DB storagekeysDb;
    private HTreeMap<ByteArrayWrapper, StorageDictionary> contracts;

    private StorageDictionaryDb() {
    }

    @PostConstruct
    StorageDictionaryDb init() {
        storagekeysDb = mapDBFactory.createTransactionalDB("metadata/keydictionary");
        contracts = storagekeysDb.hashMapCreate("contracts")
                .valueSerializer(SERIALIZER)
                .makeOrGet();
        return this;
    }

    public void close() {
        storagekeysDb.close();
    }

    public StorageDictionary get(byte[] contractAddress) {
        return contracts.get(new ByteArrayWrapper(contractAddress));
    }

    public StorageDictionary getOrCreate(byte[] contractAddress) {
        StorageDictionary ret = get(contractAddress);
        if (ret == null) {
            ret = new StorageDictionary();
            put(contractAddress, ret);
        }
        return ret;
    }

    public void put(byte[] contractAddress, StorageDictionary keys) {
        logger.debug("Update storage dictionary for contract " + Hex.toHexString(contractAddress));
        if (!keys.isValid()) {
            contracts.put(new ByteArrayWrapper(contractAddress), keys);
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

            contracts.put(new ByteArrayWrapper(contractAddress), keys);
            storagekeysDb.commit();

            keys.validate();
        }
    }
}

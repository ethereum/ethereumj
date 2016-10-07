package org.ethereum.db;

import org.ethereum.config.CommonConfig;
import org.ethereum.core.AccountState;
import org.ethereum.datasource.CachingDataSource;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.trie.JournalPruneDataSource;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    CommonConfig commonConfig = CommonConfig.getDefault();

    private static final Logger gLogger = LoggerFactory.getLogger("general");

    @Autowired @Qualifier("stateDS")
    public KeyValueDataSource dataSource;

    @Autowired
    public RepositoryImpl repository;

//    private KeyValueDataSource detailsDS;
//    private KeyValueDataSource storageDS;
//    private CachingDataSource storageDSCache;
//    private JournalPruneDataSource storageDSPrune;

    private Map<ByteArrayWrapper, ContractDetails> cache = new ConcurrentHashMap<>();
    private Set<ByteArrayWrapper> removes = new HashSet<>();

    public DetailsDataStore() {
    }

    @Autowired
    public DetailsDataStore(final CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
//        KeyValueDataSource detailsDS = commonConfig.keyValueDataSource();
//        detailsDS.setName("details");
//        detailsDS.init();
//        KeyValueDataSource storageDS = commonConfig.keyValueDataSource();
//        storageDS.setName("storage");
//        storageDS.init();
//        withDb(detailsDS, storageDS);
    }

//    public DetailsDataStore withDb(KeyValueDataSource detailsDS, KeyValueDataSource storageDS) {
//        this.detailsDS = detailsDS;
//        this.storageDS = storageDS;
//        this.storageDSCache = new CachingDataSource(storageDS);
//        this.storageDSPrune = new JournalPruneDataSource(storageDSCache);
//        return this;
//    }

    public synchronized ContractDetails get(byte[] key) {

        ByteArrayWrapper wrappedKey = wrap(key);
        ContractDetails details = cache.get(wrappedKey);

        if (details == null) {

            if (removes.contains(wrappedKey)) return null;

            AccountState accountState = repository.getAccountState(key);
            if (accountState == null) return null;

            ContractDetailsImpl detailsImpl = commonConfig.contractDetailsImpl();
            detailsImpl.setAccountState(accountState);
            detailsImpl.setAddress(key);
            details = detailsImpl;

            cache.put(wrappedKey, details);
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

//        syncLargeStorage();

//        Map<byte[], byte[]> batch = new HashMap<>();
//        for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : cache.entrySet()) {
//            ContractDetails details = entry.getValue();
//
//            byte[] key = entry.getKey().getData();
//            byte[] value = details.getEncoded();
//
//            batch.put(key, value);
//            totalSize += value.length;
//        }
//
//        detailsDS.updateBatch(batch);
//        storageDSCache.flush();
//
//        for (ByteArrayWrapper key : removes) {
//            detailsDS.delete(key.getData());
//        }

        cache.clear();
        removes.clear();

        return 0;
    }

//    public void syncLargeStorage() {
//        for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : cache.entrySet()) {
//            ContractDetails details = entry.getValue();
//            details.syncStorage();
//        }
//    }
//
//    public JournalPruneDataSource getStorageDSPrune() {
//        return storageDSPrune;
//    }
//
//    public synchronized Set<ByteArrayWrapper> keys() {
//        Set<ByteArrayWrapper> keys = new HashSet<>();
//        keys.addAll(cache.keySet());
//        keys.addAll(Utils.dumpKeys(detailsDS));
//
//        return keys;
//    }
//

//    private void temporarySave(String addr, byte[] data){
//        try {
//            FileOutputStream fos = new FileOutputStream(addr);
//            fos.write(data);
//            fos.close();
//            System.out.println("drafted: " + addr);
//        } catch (IOException e) {e.printStackTrace();}
//    }
//
//    @PreDestroy
    public synchronized void close() {
//        try {
//            gLogger.info("Closing DetailsDataStore");
//            detailsDS.close();
//            storageDS.close();
//        } catch (Exception e) {
//            gLogger.warn("Problems closing DetailsDataStore", e);
//        }
    }
}

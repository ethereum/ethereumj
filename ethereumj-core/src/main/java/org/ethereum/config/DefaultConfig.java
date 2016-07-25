package org.ethereum.config;

import org.ethereum.datasource.CachingDataSource;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.TransactionStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ethereum.db.IndexedBlockStore.BLOCK_INFO_SERIALIZER;

/**
 *
 * @author Roman Mandeleil
 * Created on: 27/01/2015 01:05
 */
@Configuration
@Import(CommonConfig.class)
public class DefaultConfig {
    private static Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    ApplicationContext appCtx;

    @Autowired
    CommonConfig commonConfig;

    @Autowired
    SystemProperties config;

    @PostConstruct
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error("Uncaught exception", e);
            }
        });
    }

    @Bean
    public BlockStore blockStore(){
        KeyValueDataSource index = commonConfig.keyValueDataSource();
        index.setName("index");
        index.init();
        KeyValueDataSource blocks = commonConfig.keyValueDataSource();
        blocks.setName("block");
        blocks.init();
        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(new CachingDataSource(index), new CachingDataSource(blocks));

        return indexedBlockStore;
    }

    @Bean
    public TransactionStore transactionStore() {
        KeyValueDataSource ds = commonConfig.keyValueDataSource();
        ds.setName("transactions");
        ds.init();
        CachingDataSource cachingDataSource = new CachingDataSource(ds);
        return new TransactionStore(cachingDataSource);
    }
}

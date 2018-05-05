/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.config;

import org.ethereum.core.*;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.*;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.datasource.rocksdb.RocksDbDataSource;
import org.ethereum.db.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.eth.handler.Eth63;
import org.ethereum.sync.FastSyncManager;
import org.ethereum.validator.*;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.ProgramPrecompile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@Configuration
@EnableTransactionManagement
@ComponentScan(
        basePackages = "org.ethereum",
        excludeFilters = @ComponentScan.Filter(NoAutoscan.class))
public class CommonConfig {
    private static final Logger logger = LoggerFactory.getLogger("general");
    private Set<DbSource> dbSources = new HashSet<>();

    private static CommonConfig defaultInstance;

    public static CommonConfig getDefault() {
        if (defaultInstance == null && !SystemProperties.isUseOnlySpringConfig()) {
            defaultInstance = new CommonConfig() {
                @Override
                public Source<byte[], ProgramPrecompile> precompileSource() {
                    return null;
                }
            };
        }
        return defaultInstance;
    }

    @Bean
    public SystemProperties systemProperties() {
        return SystemProperties.getSpringDefault();
    }

    @Bean
    BeanPostProcessor initializer() {
        return new Initializer();
    }


    @Bean @Primary
    public Repository repository() {
        return new RepositoryWrapper();
    }

    @Bean
    public Repository defaultRepository() {
        return new RepositoryRoot(stateSource(), null);
    }

    @Bean @Scope("prototype")
    public Repository repository(byte[] stateRoot) {
        return new RepositoryRoot(stateSource(), stateRoot);
    }

    /**
     * A source of nodes for state trie and all contract storage tries. <br/>
     * This source provides contract code too. <br/><br/>
     *
     * Picks node by 16-bytes prefix of its key. <br/>
     * Within {@link NodeKeyCompositor} this source is a part of ref counting workaround<br/><br/>
     *
     * <b>Note:</b> is eligible as a public node provider, like in {@link Eth63};
     * {@link StateSource} is intended for inner usage only
     *
     * @see NodeKeyCompositor
     * @see RepositoryRoot#RepositoryRoot(Source, byte[])
     * @see Eth63
     */
    @Bean
    public Source<byte[], byte[]> trieNodeSource() {
        DbSource<byte[]> db = blockchainDB();
        Source<byte[], byte[]> src = new PrefixLookupSource<>(db, NodeKeyCompositor.PREFIX_BYTES);
        return new XorDataSource<>(src, HashUtil.sha3("state".getBytes()));
    }

    @Bean
    public StateSource stateSource() {
        fastSyncCleanUp();
        StateSource stateSource = new StateSource(blockchainSource("state"),
                systemProperties().databasePruneDepth() >= 0);

        dbFlushManager().addCache(stateSource.getWriteCache());

        return stateSource;
    }

    @Bean
    @Scope("prototype")
    public Source<byte[], byte[]> cachedDbSource(String name) {
        AbstractCachedSource<byte[], byte[]>  writeCache = new AsyncWriteCache<byte[], byte[]>(blockchainSource(name)) {
            @Override
            protected WriteCache<byte[], byte[]> createCache(Source<byte[], byte[]> source) {
                WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(source, WriteCache.CacheType.SIMPLE);
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        }.withName(name);
        dbFlushManager().addCache(writeCache);
        return writeCache;
    }

    @Bean
    @Scope("prototype")
    public Source<byte[], byte[]> blockchainSource(String name) {
        return new XorDataSource<>(blockchainDbCache(), HashUtil.sha3(name.getBytes()));
    }

    @Bean
    public AbstractCachedSource<byte[], byte[]> blockchainDbCache() {
        WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(
                new BatchSourceWriter<>(blockchainDB()), WriteCache.CacheType.SIMPLE);
        ret.setFlushSource(true);
        return ret;
    }

    public DbSource<byte[]> keyValueDataSource(String name) {
        return keyValueDataSource(name, DbSettings.DEFAULT);
    }

    @Bean
    @Scope("prototype")
    @Primary
    public DbSource<byte[]> keyValueDataSource(String name, DbSettings settings) {
        String dataSource = systemProperties().getKeyValueDataSource();
        try {
            DbSource<byte[]> dbSource;
            if ("inmem".equals(dataSource)) {
                dbSource = new HashMapDB<>();
            } else if ("leveldb".equals(dataSource)){
                dbSource = levelDbDataSource();
            } else {
                dataSource = "rocksdb";
                dbSource = rocksDbDataSource();
            }
            dbSource.setName(name);
            dbSource.init(settings);
            dbSources.add(dbSource);
            return dbSource;
        } finally {
            logger.info(dataSource + " key-value data source created: " + name);
        }
    }

    @Bean
    @Scope("prototype")
    protected LevelDbDataSource levelDbDataSource() {
        return new LevelDbDataSource();
    }

    @Bean
    @Scope("prototype")
    protected RocksDbDataSource rocksDbDataSource() {
        return new RocksDbDataSource();
    }

    public void fastSyncCleanUp() {
        byte[] fastsyncStageBytes = blockchainDB().get(FastSyncManager.FASTSYNC_DB_KEY_SYNC_STAGE);
        if (fastsyncStageBytes == null) return; // no uncompleted fast sync
        if (!systemProperties().blocksLoader().isEmpty()) return; // blocks loader enabled

        EthereumListener.SyncState syncStage = EthereumListener.SyncState.values()[fastsyncStageBytes[0]];

        if (!systemProperties().isFastSyncEnabled() || syncStage == EthereumListener.SyncState.UNSECURE) {
            // we need to cleanup state/blocks/tranasaction DBs when previous fast sync was not complete:
            // - if we now want to do regular sync
            // - if the first fastsync stage was not complete (thus DBs are not in consistent state)

            logger.warn("Last fastsync was interrupted. Removing inconsistent DBs...");

            DbSource bcSource = blockchainDB();
            resetDataSource(bcSource);
        }
    }

    private void resetDataSource(Source source) {
        if (source instanceof DbSource) {
            ((DbSource) source).reset();
        } else {
            throw new Error("Cannot cleanup non-db Source");
        }
    }

    @Bean
    @Lazy
    public DbSource<byte[]> headerSource() {
        return keyValueDataSource("headers");
    }

    @Bean
    @Lazy
    public HeaderStore headerStore() {
        DbSource<byte[]> dataSource = headerSource();

        WriteCache.BytesKey<byte[]> cache = new WriteCache.BytesKey<>(
                new BatchSourceWriter<>(dataSource), WriteCache.CacheType.SIMPLE);
        cache.setFlushSource(true);
        dbFlushManager().addCache(cache);

        HeaderStore headerStore = new HeaderStore();
        Source<byte[], byte[]> headers = new XorDataSource<>(cache, HashUtil.sha3("header".getBytes()));
        Source<byte[], byte[]> index = new XorDataSource<>(cache, HashUtil.sha3("index".getBytes()));
        headerStore.init(index, headers);

        return headerStore;
    }

    @Bean
    public Source<byte[], ProgramPrecompile> precompileSource() {

        StateSource source = stateSource();
        return new SourceCodec<byte[], ProgramPrecompile, byte[], byte[]>(source,
                new Serializer<byte[], byte[]>() {
                    public byte[] serialize(byte[] object) {
                        DataWord ret = new DataWord(object);
                        ret.add(new DataWord(1));
                        return ret.getLast20Bytes();
                    }
                    public byte[] deserialize(byte[] stream) {
                        throw new RuntimeException("Shouldn't be called");
                    }
                }, new Serializer<ProgramPrecompile, byte[]>() {
                    public byte[] serialize(ProgramPrecompile object) {
                        return object == null ? null : object.serialize();
                    }
                    public ProgramPrecompile deserialize(byte[] stream) {
                        return stream == null ? null : ProgramPrecompile.deserialize(stream);
                    }
        });
    }

    @Bean
    public DbSource<byte[]> blockchainDB() {
        DbSettings settings = DbSettings.newInstance()
                .withMaxOpenFiles(systemProperties().getConfig().getInt("database.maxOpenFiles"))
                .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

        return keyValueDataSource("blockchain", settings);
    }

    @Bean
    public DbFlushManager dbFlushManager() {
        return new DbFlushManager(systemProperties(), dbSources, blockchainDbCache());
    }

    @Bean
    public BlockHeaderValidator headerValidator() {

        List<BlockHeaderRule> rules = new ArrayList<>(asList(
                new GasValueRule(),
                new ExtraDataRule(systemProperties()),
                new ProofOfWorkRule(),
                new GasLimitRule(systemProperties()),
                new BlockHashRule(systemProperties())
        ));

        return new BlockHeaderValidator(rules);
    }

    @Bean
    public ParentBlockHeaderValidator parentHeaderValidator() {

        List<DependentBlockHeaderRule> rules = new ArrayList<>(asList(
                new ParentNumberRule(),
                new DifficultyRule(systemProperties()),
                new ParentGasLimitRule(systemProperties())
        ));

        return new ParentBlockHeaderValidator(rules);
    }

    @Bean
    @Lazy
    public PeerSource peerSource() {
        DbSource<byte[]> dbSource = keyValueDataSource("peers");
        dbSources.add(dbSource);
        return new PeerSource(dbSource);
    }
}

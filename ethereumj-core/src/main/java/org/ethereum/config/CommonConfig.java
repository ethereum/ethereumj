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
import org.ethereum.db.*;
import org.ethereum.listener.EthereumListener;
import org.ethereum.sync.FastSyncManager;
import org.ethereum.validator.*;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.ProgramPrecompile;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
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


    @Bean
    public StateSource stateSource() {
        fastSyncCleanUp();
        StateSource stateSource = new StateSource(blockchainSource("state"),
                systemProperties().databasePruneDepth() >= 0, systemProperties().getConfig().getInt("cache.maxStateBloomSize") << 20);

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

    @Bean
    @Scope("prototype")
    @Primary
    public DbSource<byte[]> keyValueDataSource(String name) {
        String dataSource = systemProperties().getKeyValueDataSource();
        try {
            DbSource<byte[]> dbSource;
            if ("inmem".equals(dataSource)) {
                dbSource = new HashMapDB<>();
            } else {
                dataSource = "leveldb";
                dbSource = levelDbDataSource();
            }
            dbSource.setName(name);
            dbSource.init();
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

    public void fastSyncCleanUp() {
        byte[] fastsyncStageBytes = blockchainDB().get(FastSyncManager.FASTSYNC_DB_KEY_SYNC_STAGE);
        if (fastsyncStageBytes == null) return; // no uncompleted fast sync

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
        if (source instanceof LevelDbDataSource) {
            ((LevelDbDataSource) source).reset();
        } else {
            throw new Error("Cannot cleanup non-LevelDB database");
        }
    }

    @Bean
    @Lazy
    public DataSourceArray<BlockHeader> headerSource() {
        DbSource<byte[]> dataSource = keyValueDataSource("headers");
        BatchSourceWriter<byte[], byte[]> batchSourceWriter = new BatchSourceWriter<>(dataSource);
        WriteCache.BytesKey<byte[]> writeCache = new WriteCache.BytesKey<>(batchSourceWriter, WriteCache.CacheType.SIMPLE);
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        writeCache.setFlushSource(true);
        ObjectDataSource<BlockHeader> objectDataSource = new ObjectDataSource<>(dataSource, Serializers.BlockHeaderSerializer, 0);
        DataSourceArray<BlockHeader> dataSourceArray = new DataSourceArray<>(objectDataSource);
        return dataSourceArray;
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
        return keyValueDataSource("blockchain");
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

package org.ethereum.config;

import org.ethereum.core.*;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.*;
import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.BlockStore;
import org.ethereum.db.StateSource;
import org.ethereum.listener.EthereumListener;
import org.ethereum.sync.FastSyncManager;
import org.ethereum.validator.*;
import org.ethereum.vm.VM;
import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Configuration
@EnableTransactionManagement
@ComponentScan(
        basePackages = "org.ethereum",
        excludeFilters = @ComponentScan.Filter(NoAutoscan.class))
public class CommonConfig {
    private static final Logger logger = LoggerFactory.getLogger("general");

    private static CommonConfig defaultInstance;

    public static CommonConfig getDefault() {
        if (defaultInstance == null && !SystemProperties.isUseOnlySpringConfig()) {
            defaultInstance = new CommonConfig();
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
        return new RepositoryRoot(stateSource());
    }

    @Bean @Scope("prototype")
    public Repository repository(byte[] stateRoot) {
        return new RepositoryRoot(stateSource(), stateRoot);
    }


    @Bean
    public StateSource stateSource() {
        DbSource<byte[]> stateDS = stateDS();
        fastSyncCleanUp();
        StateSource stateSource = new StateSource(stateDS,
                systemProperties().databasePruneDepth() >= 0);

        dbFlushManager().addCache(stateSource.getWriteCache());

        return stateSource;
    }

    @Bean
    @Scope("prototype")
    public Source<byte[], byte[]> cachedDbSource(String name) {
        DbSource<byte[]> dataSource = keyValueDataSource();
        dataSource.setName(name);
        dataSource.init();
        BatchSourceWriter<byte[], byte[]> batchSourceWriter = new BatchSourceWriter<>(dataSource);
        WriteCache.BytesKey<byte[]> writeCache = new WriteCache.BytesKey<>(batchSourceWriter, WriteCache.CacheType.SIMPLE);
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        writeCache.setFlushSource(true);
        dbFlushManager().addCache(writeCache);
        return writeCache;
    }

    @Bean
    @Scope("prototype")
    @Primary
    public DbSource<byte[]> keyValueDataSource() {
        String dataSource = systemProperties().getKeyValueDataSource();
        try {
            if ("mapdb".equals(dataSource)) {
                return mapDBFactory().createDataSource();
            } else {
                dataSource = "leveldb";
                return new LevelDbDataSource();
            }
        } finally {
            logger.info(dataSource + " key-value data source created.");
        }
    }

    public void fastSyncCleanUp() {
        DbSource<byte[]> state = stateDS();

        byte[] fastsyncStageBytes = state.get(FastSyncManager.FASTSYNC_DB_KEY_SYNC_STAGE);
        if (fastsyncStageBytes == null) return; // no uncompleted fast sync

        EthereumListener.SyncState syncStage = EthereumListener.SyncState.values()[fastsyncStageBytes[0]];

        if (!systemProperties().isFastSyncEnabled() || syncStage == EthereumListener.SyncState.UNSECURE) {
            // we need to cleanup state/blocks/tranasaction DBs when previous fast sync was not complete:
            // - if we now want to do regular sync
            // - if the first fastsync stage was not complete (thus DBs are not in consistent state)

            logger.warn("Last fastsync was interrupted. Removing inconsistent DBs...");

            logger.warn("Removing tx data...");
            DbSource txSource = keyValueDataSource();
            txSource.setName("transactions");
            txSource.init();
            resetDataSource(txSource);
            txSource.close();

            logger.warn("Removing block data...");
            DbSource blockSource = keyValueDataSource();
            blockSource.setName("block");
            blockSource.init();
            resetDataSource(blockSource);
            blockSource.close();

            logger.warn("Removing index data...");
            DbSource indexSource = keyValueDataSource();
            indexSource.setName("index");
            indexSource.init();
            resetDataSource(indexSource);
            indexSource.close();

            logger.warn("Removing state data...");
            resetDataSource(state);
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
        DbSource<byte[]> dataSource = keyValueDataSource();
        dataSource.setName("headers");
        dataSource.init();
        BatchSourceWriter<byte[], byte[]> batchSourceWriter = new BatchSourceWriter<>(dataSource);
        WriteCache.BytesKey<byte[]> writeCache = new WriteCache.BytesKey<>(batchSourceWriter, WriteCache.CacheType.SIMPLE);
        writeCache.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
        writeCache.setFlushSource(true);
        ObjectDataSource<BlockHeader> objectDataSource = new ObjectDataSource<>(dataSource, Serializers.BlockHeaderSerializer, 0);
        DataSourceArray<BlockHeader> dataSourceArray = new DataSourceArray<>(objectDataSource);
        return dataSourceArray;
    }

    @Bean
    public DbSource<byte[]> stateDS() {
        DbSource<byte[]> ret = keyValueDataSource();
        ret.setName("state");
        ret.init();

        return ret;
    }

    @Bean
    public DbFlushManager dbFlushManager() {
        return new DbFlushManager(systemProperties());
    }

    @Bean
    @Scope("prototype")
    public TransactionExecutor transactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
                                                   ProgramInvokeFactory programInvokeFactory, Block currentBlock,
                                                   EthereumListener listener, long gasUsedInTheBlock) {
        return new TransactionExecutor(tx, coinbase, track, blockStore, programInvokeFactory,
                currentBlock, listener, gasUsedInTheBlock);
    }

    @Bean
    @Scope("prototype")
    public VM vm() {
        return new VM(systemProperties());
    }

    @Bean
    @Scope("prototype")
    public Program program(byte[] ops, ProgramInvoke programInvoke, Transaction transaction) {
        return new Program(ops, programInvoke, transaction, systemProperties());
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
    public MapDBFactory mapDBFactory() {
        return new MapDBFactoryImpl();
    }
}

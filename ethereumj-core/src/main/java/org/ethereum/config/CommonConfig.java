package org.ethereum.config;

import org.ethereum.core.*;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ContractDetailsImpl;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.db.RepositoryTrack;
import org.ethereum.listener.EthereumListener;
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

import java.util.*;

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

    @Bean
    @Primary
    Repository repository() {
        return new RepositoryImpl();
    }

    @Bean
    @Scope("prototype")
    @Primary
    public KeyValueDataSource keyValueDataSource() {
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
    @Scope("prototype")
    public ContractDetailsImpl contractDetailsImpl() {
        return new ContractDetailsImpl();
    }

    @Bean
    @Scope("prototype")
    public RepositoryTrack repositoryTrack(Repository parent) {
        return new RepositoryTrack(parent);
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

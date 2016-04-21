package org.ethereum.config;

import org.ethereum.core.PendingTransaction;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.redis.RedisConnection;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.validator.*;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
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

    @Autowired
    private RedisConnection redisConnection;
    @Autowired
    private MapDBFactory mapDBFactory;
    @Autowired
    private SystemProperties config;

    public CommonConfig() { }

    public CommonConfig(SystemProperties config, RedisConnection redisConnection, MapDBFactory mapDBFactory) {
        this.config = config;
        this.redisConnection = redisConnection;
        this.mapDBFactory = mapDBFactory;
    }
    @Bean
    Repository repository() {
        return new RepositoryImpl(config, keyValueDataSource(), keyValueDataSource());
    }

    @Bean
    @Scope("prototype")
    public KeyValueDataSource keyValueDataSource() {
        String dataSource = config.getKeyValueDataSource();
        try {
            if ("redis".equals(dataSource) && redisConnection.isAvailable()) {
                // Name will be defined before initialization
                return redisConnection.createDataSource("");
            } else if ("mapdb".equals(dataSource)) {
                return mapDBFactory.createDataSource();
            }

            dataSource = "leveldb";
            return new LevelDbDataSource(config);
        } finally {
            logger.info(dataSource + " key-value data source created.");
        }
    }

    @Bean
    public Set<PendingTransaction> wireTransactions() {
        String storage = "Redis";
        try {
            if (redisConnection.isAvailable()) {
                return redisConnection.createPendingTransactionSet("wireTransactions");
            }

            storage = "In memory";
            return Collections.synchronizedSet(new HashSet<PendingTransaction>());
        } finally {
            logger.info(storage + " 'wireTransactions' storage created.");
        }
    }

    @Bean
    public List<Transaction> pendingStateTransactions() {
        return Collections.synchronizedList(new ArrayList<Transaction>());
    }

    @Bean
    @Lazy
    public SessionFactory sessionFactory() {
        LocalSessionFactoryBuilder builder =
                new LocalSessionFactoryBuilder(dataSource());
        builder.scanPackages("org.ethereum.db")
                .addProperties(getHibernateProperties());

        return builder.buildSessionFactory();
    }

    private Properties getHibernateProperties() {

        Properties prop = new Properties();

        if (config.databaseReset())
            prop.put("hibernate.hbm2ddl.auto", "create-drop");
        else
            prop.put("hibernate.hbm2ddl.auto", "update");

        prop.put("hibernate.format_sql", "true");
        prop.put("hibernate.connection.autocommit", "false");
        prop.put("hibernate.connection.release_mode", "after_transaction");
        prop.put("hibernate.jdbc.batch_size", "1000");
        prop.put("hibernate.order_inserts", "true");
        prop.put("hibernate.order_updates", "true");

// todo: useful but annoying consider define by system.properties
//        prop.put("hibernate.show_sql", "true");
        prop.put("hibernate.dialect",
                "org.hibernate.dialect.H2Dialect");
        return prop;
    }

    @Bean
    @Lazy
    public HibernateTransactionManager txManager() {
        return new HibernateTransactionManager(sessionFactory());
    }


    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {

        logger.info("Connecting to the block store");

        System.setProperty("hsqldb.reconfig_logging", "false");

        String url =
                String.format("jdbc:h2:./%s/blockchain/blockchain.db;CACHE_SIZE=200000",
                        config.databaseDir());

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(url);
        ds.setUsername("sa");

        return ds;

    }

    @Bean
    public BlockHeaderValidator headerValidator() {

        List<BlockHeaderRule> rules = new ArrayList<>(asList(
                new GasValueRule(),
                new ExtraDataRule(),
                new ProofOfWorkRule(),
                new GasLimitRule()
        ));

        return new BlockHeaderValidator(rules);
    }

    @Bean
    public ParentBlockHeaderValidator parentHeaderValidator() {

        List<DependentBlockHeaderRule> rules = new ArrayList<>(asList(
                new ParentNumberRule(),
                new DifficultyRule(config.getBlockchainConfig()),
                new ParentGasLimitRule(config.getBlockchainConfig())
        ));

        return new ParentBlockHeaderValidator(rules);
    }

}

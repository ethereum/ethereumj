package org.ethereum.facade;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.redis.RedisConnection;
import org.ethereum.db.RepositoryImpl;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.ethereum.config.SystemProperties.CONFIG;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "org.ethereum")
public class CommonConfig {

    private static final Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    private RedisConnection redisConnection;

    @Bean
    Repository repository() {
        return new RepositoryImpl(keyValueDataSource(), keyValueDataSource());
    }

    @Bean
    @Scope("prototype")
    public KeyValueDataSource keyValueDataSource() {
        String dataSource = CONFIG.getKeyValueDataSource();
        try {
            if ("redis".equals(dataSource) && redisConnection.isAvailable()) {
                // Name will be defined before initialization
                return redisConnection.createDataSource("");
            }

            dataSource = "leveldb";
            return new LevelDbDataSource();
        } finally {
            logger.info(dataSource + " key-value data source created.");
        }
    }

    @Bean
    public Set<Transaction> pendingTransactions() {
        String storage = "Redis";
        try {
            if (redisConnection.isAvailable()) {
                return redisConnection.createTransactionSet("pendingTransactions");
            }

            storage = "In memory";
            return Collections.synchronizedSet(new HashSet<Transaction>());
        } finally {
            logger.info(storage + " 'pendingTransactions' storage created.");
        }
    }

    @Bean
    public SessionFactory sessionFactory() throws SQLException {
        LocalSessionFactoryBuilder builder =
                new LocalSessionFactoryBuilder(dataSource());
        builder.scanPackages("org.ethereum.db")
                .addProperties(getHibernateProperties());

        return builder.buildSessionFactory();
    }

    private Properties getHibernateProperties() {

        Properties prop = new Properties();

        if (SystemProperties.CONFIG.databaseReset())
            prop.put("hibernate.hbm2ddl.auto", "create");

        prop.put("hibernate.format_sql", "true");

// todo: useful but annoying consider define by system.properties
//        prop.put("hibernate.show_sql", "true");
        prop.put("hibernate.dialect",
                "org.hibernate.dialect.HSQLDialect");
        return prop;
    }


    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource());

        return dataSourceTransactionManager;
    }

    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {

        logger.info("Connecting to the block store");

        System.setProperty("hsqldb.reconfig_logging", "false");

        String url =
                String.format("jdbc:hsqldb:file:./%s/blockchain/blockchain.db;" +
                                "create=%s;hsqldb.default_table_type=cached",

                        SystemProperties.CONFIG.databaseDir(),
                        SystemProperties.CONFIG.databaseReset());

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl(url);
        ds.setUsername("sa");


        return ds;
    }

}

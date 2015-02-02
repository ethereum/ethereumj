package org.ethereum.facade;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.RedisDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.BlockStoreImpl;
import org.ethereum.db.RepositoryImpl;
import org.hibernate.Session;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Properties;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 27/01/2015 01:05
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "org.ethereum")
public class DefaultConfig {

    private static final Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    Ethereum eth;
    
    @Bean 
    Repository repository(){
        return new RepositoryImpl(keyValueDataSource(), keyValueDataSource());
    }

    @Bean
    @Scope("prototype")
    public KeyValueDataSource keyValueDataSource(){
        
        if (CONFIG.getKeyValueDataSource().equals("redis")) {
            return  new RedisDataSource();
        }

        return new LevelDbDataSource();
    }

    @Bean
    @Transactional(propagation = Propagation.SUPPORTS)
    public BlockStore blockStore(SessionFactory sessionFactory){
        return new BlockStoreImpl(sessionFactory);
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

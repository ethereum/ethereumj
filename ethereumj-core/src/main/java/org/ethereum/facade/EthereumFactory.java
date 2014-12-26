package org.ethereum.facade;

import org.ethereum.config.SystemProperties;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.shh.ShhHandler;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;
import java.util.Properties;

/**
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * Created on: 13/11/2014 11:22
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "org.ethereum")
public class EthereumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

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


    @Autowired
    Ethereum eth;

    private static ApplicationContext context;
    private static EthereumFactory factory;


    public static Ethereum createEthereum() {

        logger.info("capability eth version: [{}]", EthHandler.VERSION);
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);

        if (context == null) {
            context = new AnnotationConfigApplicationContext(EthereumFactory.class);
            factory = context.getBean(EthereumFactory.class);
        }

        return factory.eth;
    }
}

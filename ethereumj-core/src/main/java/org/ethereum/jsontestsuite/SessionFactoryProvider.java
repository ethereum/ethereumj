package org.ethereum.jsontestsuite;


import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

import java.util.Properties;

public class SessionFactoryProvider {
    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    public static SessionFactory sessionFactory() {
        LocalSessionFactoryBuilder builder =
                new LocalSessionFactoryBuilder(dataSource());
        builder.scanPackages("org.ethereum.db")
                .addProperties(getHibernateProperties());

        return builder.buildSessionFactory();
    }


    private static Properties getHibernateProperties() {

        Properties prop = new Properties();

        prop.put("hibernate.hbm2ddl.auto", "create-drop");
        prop.put("hibernate.format_sql", "true");
        prop.put("hibernate.connection.autocommit", "false");
        prop.put("hibernate.connection.release_mode", "after_transaction");
        prop.put("hibernate.jdbc.batch_size", "1000");
        prop.put("hibernate.order_inserts", "true");
        prop.put("hibernate.order_updates", "true");

        prop.put("hibernate.dialect",
                "org.hibernate.dialect.H2Dialect");

        return prop;
    }


    private static DriverManagerDataSource dataSource() {

        logger.info("Connecting to the block store");

        System.setProperty("hsqldb.reconfig_logging", "false");

        String url =
                String.format("jdbc:h2:./%s/blockchain/blockchain.db;CACHE_SIZE=10240;PAGE_SIZE=1024;LOCK_MODE=0;UNDO_LOG=0",
                        "test_mem_store_db");

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(url);
        ds.setUsername("sa");

        return ds;
    }
}

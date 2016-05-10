package org.ethereum;

import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Roman Mandeleil
 * @since 16.11.2014
 */
public class TestContext {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private SystemProperties config = SystemProperties.getDefault();

    @Bean
    public SessionFactory sessionFactory() throws SQLException {

        logger.info("loading context");

        LocalSessionFactoryBuilder builder =
                new LocalSessionFactoryBuilder(dataSource());
        builder.scanPackages("org.ethereum.db")
                .addProperties(getHibernateProperties());

        return builder.buildSessionFactory();
    }

    private Properties getHibernateProperties() {

        Properties prop = new Properties();

        if (config.databaseReset())
            prop.put("hibernate.hbm2ddl.auto", "create");

        prop.put("hibernate.format_sql", "true");

        // todo: useful but annoying consider define by system.properties
//        prop.put("hibernate.show_sql", "true");
        prop.put("hibernate.dialect",
                "org.hibernate.dialect.HSQLDialect");

        prop.put("hibernate.connection.autocommit",
                "true");

        return prop;
    }


/*
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="entityManagerFactory" />
    </bean>
*/


    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {

        logger.info("Connecting to the block store");

        System.setProperty("hsqldb.reconfig_logging", "false");

        String url =
                String.format("jdbc:hsqldb:file:./%s/blockchain/blockchain.db;" +
                                "create=%s;hsqldb.default_table_type=cached",

                        config.databaseDir(),
                        config.databaseReset());

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl(url);
        ds.setUsername("sa");

        return ds;
    }


    @Autowired
    Ethereum eth;

}

package org.ethereum.datasource;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public interface DataSource {

    void init();

    void setName(String name);
    
    String getName();

    void close();
}

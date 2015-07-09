package org.ethereum.datasource;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public interface DataSource {

    void setName(String name);

    String getName();

    void init();

    boolean isAlive();
    
    void close();
}

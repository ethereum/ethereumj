package org.ethereum.datasource;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public interface QueueDataSource extends DataSource {

    boolean offer(byte[] e);

    byte[] peek();

    byte[] poll();

    boolean isEmpty();
}

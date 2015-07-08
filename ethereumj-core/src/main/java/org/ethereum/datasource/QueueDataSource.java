package org.ethereum.datasource;

/**
 * @author Mikhail Kalinin
 * @since 07.07.2015
 */
public interface QueueDataSource extends DataSource {

    void offerFirst(byte[] e);

    byte[] peekFirst();

    byte[] pollFirst();

    void offerLast(byte[] e);

    byte[] peekLast();

    byte[] pollLast();

    boolean isEmpty();
}

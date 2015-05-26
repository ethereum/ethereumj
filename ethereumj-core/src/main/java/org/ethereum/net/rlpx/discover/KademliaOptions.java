package org.ethereum.net.rlpx.discover;

/**
 * Created by kest on 5/25/15.
 */
public class KademliaOptions {
    public static final int BUCKET_SIZE = 16;
    public static final int ALPHA = 3;
    public static final int BINS = 256;

    public static final long EVICTION_CHECK_INTERVAL = 75;
    public static final long REQ_TIMEOUT = 300;
    public static final long BUCKET_REFRESH = 7200;
}

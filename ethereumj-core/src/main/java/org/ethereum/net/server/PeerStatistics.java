package org.ethereum.net.server;

/**
 * @author Mikhail Kalinin
 * @since 29.02.2016
 */
public class PeerStatistics {

    private double avgLatency = 0;
    private long pingCount = 0;

    public void pong(long pingStamp) {
        long latency = System.currentTimeMillis() - pingStamp;
        avgLatency = ((avgLatency * pingCount) + latency) / ++pingCount;
    }

    public double getAvgLatency() {
        return avgLatency;
    }
}

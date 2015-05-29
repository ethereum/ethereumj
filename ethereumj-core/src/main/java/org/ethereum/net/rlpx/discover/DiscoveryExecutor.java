package org.ethereum.net.rlpx.discover;

import io.netty.channel.Channel;
import org.ethereum.crypto.ECKey;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscoveryExecutor {
    Channel channel;
    NodeTable table;
    ECKey key;

    ScheduledExecutorService discoverer = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor();

    DiscoveryExecutor(Channel channel, NodeTable table, ECKey key) {
        this.channel = channel;
        this.table = table;
        this.key = key;
    }

    public void discover() {

        discoverer.scheduleWithFixedDelay(
                new DiscoverTask(table.getNode().getId(), channel, key, table),
                0, KademliaOptions.DISCOVER_CYCLE, TimeUnit.SECONDS);

        refresher.scheduleWithFixedDelay(
                new RefreshTask(channel, key, table),
                0, KademliaOptions.BUCKET_REFRESH, TimeUnit.MILLISECONDS);

    }


}

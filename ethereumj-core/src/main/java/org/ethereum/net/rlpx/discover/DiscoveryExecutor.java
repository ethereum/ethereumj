package org.ethereum.net.rlpx.discover;

import io.netty.channel.Channel;
import org.ethereum.crypto.ECKey;
import org.ethereum.net.rlpx.discover.table.KademliaOptions;
import org.ethereum.net.rlpx.discover.table.NodeTable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscoveryExecutor {
//    Channel channel;
//    NodeTable table;
//    ECKey key;

    ScheduledExecutorService discoverer = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor();

    NodeManager nodeManager;

    public DiscoveryExecutor(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void discover() {

        discoverer.scheduleWithFixedDelay(
                new DiscoverTask(nodeManager),
                0, KademliaOptions.DISCOVER_CYCLE, TimeUnit.SECONDS);

        refresher.scheduleWithFixedDelay(
                new RefreshTask(nodeManager),
                0, KademliaOptions.BUCKET_REFRESH, TimeUnit.MILLISECONDS);

    }


}

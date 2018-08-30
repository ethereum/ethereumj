/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.sharding.proposer;

import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.pubsub.BeaconBlockImported;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.processing.BeaconChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ProposerService} that is based on {@link ScheduledExecutorService}.
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class ProposerServiceImpl implements ProposerService {

    private static final Logger logger = LoggerFactory.getLogger("proposer");

    BeaconProposer proposer;
    BeaconChain beaconChain;
    Publisher publisher;

    private ScheduledExecutorService proposerThread;

    public ProposerServiceImpl(BeaconProposer proposer, BeaconChain beaconChain, Publisher publisher) {
        this.proposer = proposer;
        this.beaconChain = beaconChain;
        this.publisher = publisher;
    }

    @Override
    public void init() {
        this.proposerThread = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName("beacon-proposer-thread");
            t.setDaemon(true);
            return t;
        });

        // trigger proposer each time when new block is imported
        publisher.subscribe(BeaconBlockImported.class, data ->
                this.submit(data.getBlock().getSlotNumber() + 1));

        // schedule initial slot proposing
        submit(proposer.getSlotNumber(System.currentTimeMillis()) + 2);
    }

    @Override
    public void submit(long slotNumber) {
        if (proposerThread == null) return;

        // skip slots that are in the past
        if (slotNumber < proposer.getSlotNumber(System.currentTimeMillis()))
            return;

        long delayMillis = proposer.getTimestamp(slotNumber) - System.currentTimeMillis();
        proposerThread.schedule(() -> {
            try {
                Beacon newBlock = proposer.createNewBlock(slotNumber);
                beaconChain.insert(newBlock);
                return newBlock;
            } catch (Throwable t) {
                logger.error("Failed to propose block", t);
                throw t;
            }
        }, delayMillis, TimeUnit.MILLISECONDS);

        logger.info("Schedule new block #{}, proposing in {}ms", slotNumber, delayMillis);
    }
}

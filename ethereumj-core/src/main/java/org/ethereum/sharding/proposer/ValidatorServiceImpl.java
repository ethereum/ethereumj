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

import org.ethereum.crypto.HashUtil;
import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.domain.Beacon;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.sharding.processing.state.BeaconState;
import org.ethereum.sharding.processing.state.Committee;
import org.ethereum.sharding.pubsub.BeaconBlockImported;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.processing.BeaconChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.ethereum.sharding.util.BeaconUtils.calcNextProposingSlot;
import static org.ethereum.sharding.util.BeaconUtils.getCurrentSlotNumber;
import static org.ethereum.sharding.util.BeaconUtils.getSlotStartTime;
import static org.ethereum.sharding.util.BeaconUtils.scanCommittees;

/**
 * Implementation of {@link ValidatorService} that is based on {@link ScheduledExecutorService}.
 *
 * @author Mikhail Kalinin
 * @since 28.08.2018
 */
public class ValidatorServiceImpl implements ValidatorService {

    private static final Logger logger = LoggerFactory.getLogger("proposer");

    BeaconProposer proposer;
    BeaconChain beaconChain;
    Publisher publisher;
    ValidatorConfig config;

    private ScheduledExecutorService executor;
    private Map<Integer, ScheduledFuture> currentTasks = new ConcurrentHashMap<>();
    private Map<Integer, byte[]> pubKeysMap = new ConcurrentHashMap<>();
    private Set<Integer> validatorIndices;
    private long lastStateRecalc;

    public ValidatorServiceImpl(BeaconProposer proposer, BeaconChain beaconChain,
                                Publisher publisher, ValidatorConfig config) {
        this.proposer = proposer;
        this.beaconChain = beaconChain;
        this.publisher = publisher;
        this.config = config;
    }

    @Override
    public void init(BeaconState state, byte[]... pubKeys) {
        assert pubKeys.length > 0;

        this.validatorIndices = new HashSet<>();

        for (byte[] pubKey : pubKeys) {
            Validator validator = state.getValidatorSet().getByPubKey(pubKey);
            if (validator != null) {
                this.validatorIndices.add(validator.getIndex());
                this.pubKeysMap.put(validator.getIndex(), validator.getPubKey());
            } else {
                // something went wrong
                logger.error("Failed to start proposer for {}: validator does not exist", HashUtil.shortHash(pubKey));
                return;
            }

            this.validatorIndices.add(validator.getIndex());
        }

        this.executor = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName("beacon-proposer-thread");
            t.setDaemon(true);
            return t;
        });

        this.lastStateRecalc = state.getCrystallizedState().getLastStateRecalc();
        // submit initial task
        submitIfAssigned(state.getCommittees());

        // listen to state updates
        publisher.subscribe(BeaconBlockImported.class, (data) -> {
            // trigger only if crystallized state has been recalculated
            if (data.isBest() && data.getState().getCrystallizedState().getLastStateRecalc() > lastStateRecalc) {
                this.lastStateRecalc = data.getState().getCrystallizedState().getLastStateRecalc();
                this.submitIfAssigned(data.getState().getCommittees());
            }
        });
    }

    private void submitIfAssigned(Committee[][] committees) {
        // validator from only the first committee is eligible to propose beacon chain block
        List<Committee.Index> indices = scanCommittees(validatorIndices, committees)
                .stream().filter(idx -> idx.getCommitteeIdx() == 0).collect(Collectors.toList());
        indices.sort((i1, i2) -> Integer.compare(i1.getSlotOffset(), i2.getSlotOffset()));

        for (Committee.Index index : indices) {
            // get number of the next slot that validator is eligible to propose
            long slotNumber = calcNextProposingSlot(getCurrentSlotNumber(), index.getSlotOffset());

            // not an obvious way of calculating proposer index,
            // proposer = committee[X % len(committee)], X = slotNumber
            // taken from the spec
            if (slotNumber % index.getCommitteeSize() == index.getArrayIdx()) {
                this.propose(slotNumber, index.getValidatorIdx());
            }
        }
    }

    @Override
    public void propose(long slotNumber, int validatorIdx) {
        if (executor == null) return;

        // skip slots that start in the past
        if (slotNumber <= getCurrentSlotNumber())
            return;

        // always cancel current task and create a new one
        if (currentTasks.containsKey(validatorIdx))
            currentTasks.get(validatorIdx).cancel(false);

        long delayMillis = getSlotStartTime(slotNumber) - System.currentTimeMillis();
        ScheduledFuture newTask = executor.schedule(() -> {
            try {
                Beacon newBlock = proposer.createNewBlock(slotNumber, pubKeysMap.get(validatorIdx));
                beaconChain.insert(newBlock);
                return newBlock;
            } catch (Throwable t) {
                logger.error("Failed to propose block", t);
                throw t;
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
        currentTasks.put(validatorIdx, newTask);

        logger.info("Validator {}: schedule new slot #{}, proposing in {}ms", validatorIdx, slotNumber, delayMillis);
    }
}

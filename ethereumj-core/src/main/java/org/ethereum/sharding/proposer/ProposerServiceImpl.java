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
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.ethereum.sharding.util.BeaconUtils.calcNextProposingSlot;
import static org.ethereum.sharding.util.BeaconUtils.scanCommittees;

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
    ValidatorConfig config;

    private ScheduledExecutorService proposerThread;
    private ScheduledFuture currentTask;
    private int validatorIdx;
    private long lastStateRecalc;

    public ProposerServiceImpl(BeaconProposer proposer, BeaconChain beaconChain,
                               Publisher publisher, ValidatorConfig config) {
        this.proposer = proposer;
        this.beaconChain = beaconChain;
        this.publisher = publisher;
        this.config = config;
    }

    @Override
    public void init(BeaconState state) {

        Validator validator = state.getValidatorSet().getByPupKey(config.pubKey());
        // something went wrong
        if (validator == null) {
            logger.error("Failed to start proposer: validator {} does not exist", Hex.toHexString(config.pubKey()));
            return;
        }

        this.validatorIdx = validator.getIndex();

        this.proposerThread = Executors.newSingleThreadScheduledExecutor((r) -> {
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
        Committee.Index index = scanCommittees(validatorIdx, committees);
        if (index.isEmpty())
            return;

        // validator from only the first committee is eligible to propose beacon chain block
        if (index.getCommitteeIdx() > 0)
            return;

        // get number of the next slot that validator is eligible to propose
        long slotNumber = calcNextProposingSlot(proposer.getSlotNumber(System.currentTimeMillis()),
                index.getSlotOffset());

        // not an obvious way of calculating proposer index,
        // proposer = committee[X % len(committee)], X = slotNumber
        // from chat with Hsiao and Danny
        if (slotNumber % index.getCommitteeSize() == index.getValidatorIdx()) {
            this.submit(slotNumber);
        }
    }

    @Override
    public void submit(long slotNumber) {
        if (proposerThread == null) return;

        // skip slots that are in the past
        if (slotNumber < proposer.getSlotNumber(System.currentTimeMillis()))
            return;

        // always cancel current task and create a new one
        if (currentTask != null)
            currentTask.cancel(false);

        long delayMillis = proposer.getTimestamp(slotNumber) - System.currentTimeMillis();
        currentTask = proposerThread.schedule(() -> {
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

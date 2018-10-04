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
package org.ethereum.sharding.service;

import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.util.Randao;
import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.contract.DepositContract;
import org.ethereum.sharding.crypto.DepositAuthority;
import org.ethereum.sharding.domain.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.CompletableFuture;

import static org.ethereum.sharding.proposer.BeaconProposer.SLOT_DURATION;
import static org.ethereum.sharding.pubsub.Events.onValidatorStateUpdated;
import static org.ethereum.sharding.service.ValidatorRegistrationService.State.DepositFailed;
import static org.ethereum.sharding.service.ValidatorRegistrationService.State.Enlisted;
import static org.ethereum.sharding.service.ValidatorRegistrationService.State.Undefined;
import static org.ethereum.sharding.service.ValidatorRegistrationService.State.WaitForDeposit;

/**
 * Default implementation of {@link ValidatorRegistrationService}
 *
 * @author Mikhail Kalinin
 * @since 21.07.2018
 */
public class ValidatorRegistrationServiceImpl implements ValidatorRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    private static final int RANDAO_ROUNDS = 30 * 24 * 3600 / (int) (SLOT_DURATION / 1000); // 30 days of block proposing in solo mode

    Ethereum ethereum;
    DepositContract depositContract;
    ValidatorConfig config;
    Randao randao;
    DepositAuthority depositAuthority;
    Publisher publisher;

    private State state = Undefined;

    public ValidatorRegistrationServiceImpl(Ethereum ethereum, ValidatorConfig config, DepositContract depositContract,
                                            DepositAuthority depositAuthority, Randao randao, Publisher publisher) {
        assert config.isEnabled();

        this.ethereum = ethereum;
        this.config = config;
        this.depositContract = depositContract;
        this.depositAuthority = depositAuthority;
        this.randao = randao;
        this.publisher = publisher;
    }

    @Override
    public void init() {
        if (isEnlisted()) {
            updateState(Enlisted);
            return;
        }

        updateState(WaitForDeposit);

        ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void onSyncDone(SyncState state) {
                if (!isEnlisted()) {
                    byte[] commitment = initRandao();
                    logger.info("Generate RANDAO with commitment: {}", Hex.toHexString(commitment));
                    deposit(commitment);
                }
            }
        });
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public byte[][] pubKeys() {
        return new byte[][] { config.pubKey() };
    }

    byte[] initRandao() {
        // generate randao images
        return randao.generate(RANDAO_ROUNDS);
    }

    void deposit(byte[] randao) {
        CompletableFuture<Validator> future = depositContract.deposit(
                config.pubKey(), config.withdrawalShard(), config.withdrawalAddress(),
                randao, depositAuthority);

        future.whenCompleteAsync((validator, t) -> {
            if (validator != null) {
                updateState(Enlisted);
            } else {
                logger.error("Validator: {}, deposit failed with error: {}",
                        HashUtil.shortHash(config.pubKey()), t.getMessage());
                updateState(DepositFailed);
            }
        });
    }

    void updateState(State newState) {
        state = newState;
        publisher.publish(onValidatorStateUpdated(newState));
        logState();
    }

    void logState() {
        logger.info("Validator: {}, state: {}", HashUtil.shortHash(config.pubKey()), state);
    }

    boolean isEnlisted() {
        return depositContract.usedPubKey(config.pubKey());
    }
}

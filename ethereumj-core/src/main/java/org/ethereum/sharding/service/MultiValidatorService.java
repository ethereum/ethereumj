package org.ethereum.sharding.service;

import org.ethereum.crypto.HashUtil;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.contract.DepositContract;
import org.ethereum.sharding.crypto.DepositAuthority;
import org.ethereum.sharding.domain.BeaconGenesis;
import org.ethereum.sharding.domain.Validator;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.util.Randao;
import org.ethereum.util.ByteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.ethereum.sharding.proposer.BeaconProposer.SLOT_DURATION;
import static org.ethereum.sharding.pubsub.Events.onValidatorStateUpdated;
import static org.ethereum.sharding.service.ValidatorService.State.DepositFailed;
import static org.ethereum.sharding.service.ValidatorService.State.Enlisted;
import static org.ethereum.sharding.service.ValidatorService.State.Undefined;
import static org.ethereum.sharding.service.ValidatorService.State.WaitForDeposit;

/**
 * @author Mikhail Kalinin
 * @since 27.09.2018
 */
public class MultiValidatorService implements ValidatorService {

    private static final Logger logger = LoggerFactory.getLogger("beacon");

    private static final int RANDAO_ROUNDS = 30 * 24 * 3600 / (int) (SLOT_DURATION / 1000); // 30 days of block proposing in solo mode

    Ethereum ethereum;
    DepositContract depositContract;
    ValidatorConfig config;
    Randao randao;
    DepositAuthority depositAuthority;
    Publisher publisher;

    private State state = Undefined;
    private List<byte[]> pubKeys;

    public MultiValidatorService(Ethereum ethereum, ValidatorConfig config, DepositContract depositContract,
                                DepositAuthority depositAuthority, Randao randao, Publisher publisher) {
        assert config.isEnabled();

        this.ethereum = ethereum;
        this.config = config;
        this.depositContract = depositContract;
        this.depositAuthority = depositAuthority;
        this.randao = randao;
        this.publisher = publisher;
        this.pubKeys = fetchPubKeys();
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
        return pubKeys.toArray(new byte[pubKeys.size()][]);
    }

    byte[] initRandao() {
        // generate randao images
        return randao.generate(RANDAO_ROUNDS);
    }

    void deposit(byte[] randao) {
        Set<byte[]> toRegistration = Collections.synchronizedSet(new ByteArraySet());
        toRegistration.addAll(notYetDeposited());

        for (byte[] pubKey : notYetDeposited()) {
            CompletableFuture<Validator> future = depositContract.deposit(
                    pubKey, config.withdrawalShard(), config.withdrawalAddress(),
                    randao, depositAuthority);

            future.whenCompleteAsync((validator, t) -> {
                if (validator != null) {
                    toRegistration.remove(validator.getPubKey());
                    logState(pubKey, Enlisted);

                    // update service state only if all validators has been registered
                    if (toRegistration.isEmpty()) {
                        updateState(Enlisted);
                    }
                } else {
                    logger.error("Validator: {}, deposit failed with error: {}",
                            HashUtil.shortHash(config.pubKey()), t.getMessage());
                    updateState(DepositFailed);
                }
            });
        }
    }

    synchronized void updateState(State newState) {
        if (state == newState) return;
        state = newState;
        publisher.publish(onValidatorStateUpdated(newState));
        logger.info("Validator service state: {}", state);
    }

    void logState(byte[] pubKey, State state) {
        logger.info("Validator: {}, state: {}", HashUtil.shortHash(pubKey), state);
    }

    List<byte[]> notYetDeposited() {
        List<byte[]> ret = new ArrayList<>();
        for (byte[] pubKey : pubKeys) {
            if (!depositContract.usedPubKey(pubKey)) {
                ret.add(pubKey);
            }
        }
        return ret;
    }

    boolean isEnlisted() {
        for (byte[] pubKey : pubKeys) {
            if (!depositContract.usedPubKey(pubKey)) {
                return false;
            }
        }
        return true;
    }

    List<byte[]> fetchPubKeys() {
        return BeaconGenesis.instance().getInitialValidators()
                .stream().map(Hex::decode).collect(Collectors.toList());
    }
}

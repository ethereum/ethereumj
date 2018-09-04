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
package org.ethereum.sharding.manager;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.BlockStore;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.RepositoryWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.WorldManager;
import org.ethereum.sharding.config.DepositContractConfig;
import org.ethereum.sharding.processing.BeaconChain;
import org.ethereum.sharding.proposer.ProposerService;
import org.ethereum.sharding.pubsub.BeaconChainSynced;
import org.ethereum.sharding.pubsub.Publisher;
import org.ethereum.sharding.pubsub.ValidatorStateUpdated;
import org.ethereum.sharding.service.ValidatorService;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import static org.ethereum.util.ByteUtil.longToBytes;

/**
 * In addition to original {@link WorldManager} runs sharding bootstrap things:
 * <ul>
 *     <li> deploys validator registration contract to the empty blockchain
 *     <li> starts {@link ValidatorService} if it's enabled
 * </ul>
 *
 * @author Mikhail Kalinin
 * @since 26.07.2018
 */
public class ShardingWorldManager extends WorldManager {

    private static final Logger logger = LoggerFactory.getLogger("sharding");

    private static final ECKey DEPLOY_AUTHORITY = ECKey.fromPrivate(Hex.decode("41791102999c339c844880b23950704cc43aa840f3739e365323cda4dfa89e7a"));
    private static final long DEPLOY_GAS_PRICE = 1_000_000_000; // 1 GWEI
    private static final long DEPLOY_GAS_LIMIT = 200_000;

    DepositContractConfig contractConfig;
    DbFlushManager dbFlushManager;
    DbFlushManager beaconDbFlusher;
    Publisher publisher;
    ValidatorService validatorService;

    private CompletableFuture<Void> contractInit = new CompletableFuture<>();
    private CompletableFuture<ValidatorService.State> validatorServiceInit = new CompletableFuture<>();

    public ShardingWorldManager(SystemProperties config, Repository repository, EthereumListener listener,
                                Blockchain blockchain, BlockStore blockStore, DepositContractConfig contractConfig,
                                DbFlushManager dbFlushManager) {
        super(config, repository, listener, blockchain, blockStore);
        this.contractConfig = contractConfig;
        this.dbFlushManager = dbFlushManager;
    }

    @Override
    protected void init() {
        initDepositContract();
        syncManager.init(getChannelManager(), pool);
    }

    private void initDepositContract() {
        if (getBlockchain().getBestBlock().isGenesis()) {
            Block best = getBlockchain().getBestBlock();

            // build and execute deploy transaction
            BigInteger nonce = getRepository().getNonce(DEPLOY_AUTHORITY.getAddress());
            Integer chainId = config.getBlockchainConfig().getConfigForBlock(0).getChainId();
            Transaction tx = new Transaction(nonce.toByteArray(), longToBytes(DEPLOY_GAS_PRICE), longToBytes(DEPLOY_GAS_LIMIT),
                    null, BigInteger.ZERO.toByteArray(), contractConfig.getBin(), chainId) {
                @Override
                public byte[] getContractAddress() {
                    return contractConfig.getAddress();
                }
            };
            tx.sign(DEPLOY_AUTHORITY);

            TransactionExecutor executor = new TransactionExecutor(
                    tx, best.getCoinbase(), (Repository) getRepository(),
                    getBlockStore(), new ProgramInvokeFactoryImpl(), best, new EthereumListenerAdapter(), 0);

            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            RepositoryWrapper repository = ((RepositoryWrapper) getRepository());
            repository.flush();

            // Update Genesis root
            Block genesis = getBlockchain().getBestBlock();
            genesis.setStateRoot(repository.getRoot());
            getBlockStore().saveBlock(genesis, genesis.getDifficultyBI(), true);
            getBlockchain().setBestBlock(genesis);

            dbFlushManager.flushSync();

            logger.info("Set Validator Registration: contract.address: {}", Hex.toHexString(contractConfig.getAddress()));
        }
        contractInit.complete(null);
    }

    public void setValidatorService(final ValidatorService validatorService) {
        this.validatorService = validatorService;
        if (validatorService.getState() == ValidatorService.State.Disabled) {
            validatorServiceInit.complete(ValidatorService.State.Disabled);
        } else {
            publisher.subscribe(ValidatorStateUpdated.class, data -> {
                if (data.getNewState() == ValidatorService.State.Enlisted ||
                        data.getNewState() == ValidatorService.State.DepositFailed) {
                    validatorServiceInit.complete(data.getNewState());
                }
            });
        }
        contractInit.thenRunAsync(this.validatorService::init);
    }

    public void setProposerService(final ProposerService proposerService) {
        // start only if validator is enlisted and after sync is finished
        publisher.subscribe(BeaconChainSynced.class, data -> {
            if (validatorService.getState() == ValidatorService.State.Enlisted) {
                proposerService.init();
            }
        });
    }

    public void setBeaconChain(final BeaconChain beaconChain) {
        // PoC mode:
        // wait for validator initialization
        // it's needed to be sure that the validator is registered before beacon genesis state calculation
        validatorServiceInit.runAfterBothAsync(contractInit, beaconChain::init);
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public void setBeaconDbFlusher(final DbFlushManager beaconDbFlusher) {
        this.beaconDbFlusher = beaconDbFlusher;
    }

    @Override
    public void close() {
        if (beaconDbFlusher != null) {
            logger.info("close: beacon chain flush manager ...");
            beaconDbFlusher.close();
        }

        super.close();
    }
}

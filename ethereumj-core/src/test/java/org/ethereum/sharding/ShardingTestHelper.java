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
package org.ethereum.sharding;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.datasource.WriteCache;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.DbFlushManager;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.sharding.config.DepositContractConfig;
import org.ethereum.sharding.config.ValidatorConfig;
import org.ethereum.sharding.contract.DepositContract;
import org.ethereum.sharding.crypto.DepositAuthority;
import org.ethereum.sharding.crypto.UnsecuredDepositAuthority;
import org.ethereum.sharding.service.ValidatorRepository;
import org.ethereum.sharding.service.ValidatorRepositoryImpl;
import org.ethereum.sharding.service.ValidatorService;
import org.ethereum.sharding.service.ValidatorServiceImpl;
import org.ethereum.sharding.util.Randao;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Mikhail Kalinin
 * @since 28.07.2018
 */
public class ShardingTestHelper {

    public static ShardingBootstrap bootstrap() {
        ShardingBootstrap sharding = new ShardingBootstrap();

        DepositContractConfig contractConfig = DepositContractConfig.fromFile();

        SystemProperties systemProperties = SystemProperties.getDefault();
        systemProperties.setGenesisInfo("sharding-test.json");
        systemProperties.setMineMinGasPrice(BigInteger.ONE);
        Genesis genesis = Genesis.getInstance(systemProperties);

        StandaloneBlockchain standaloneBlockchain = sharding.standaloneBlockchain = new StandaloneBlockchain()
                .withGenesis(genesis)
                .withNetConfig(StandaloneBlockchain.getEasyMiningConfig())
                .withMinerCoinbase(Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826"))
                .withListener(new CompositeEthereumListener() {
                    @Override
                    public void onSyncDone(SyncState state) {
                        listeners.forEach(listener -> listener.onSyncDone(state));
                    }
                });
        standaloneBlockchain.setSender(
                ECKey.fromPrivate(Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4")));
        standaloneBlockchain.getBlockchain();

        DbFlushManager dbFlushManager = sharding.flushManager = new DbFlushManager(systemProperties, Collections.emptySet(),
                new WriteCache.BytesKey<>(standaloneBlockchain.getStateDS(), WriteCache.CacheType.SIMPLE)) {
            @Override
            public synchronized Future<Boolean> flush() {
                CompletableFuture<Boolean> ret = new CompletableFuture<>();
                ret.complete(true);
                return ret;
            }
        };
        Ethereum ethereum = sharding.ethereum = new EthereumImpl(systemProperties, standaloneBlockchain.getListener()) {
            @Override
            public Future<Transaction> submitTransaction(Transaction transaction) {
                sharding.standaloneBlockchain.submitTransaction(transaction);
                CompletableFuture<Transaction> ret = new CompletableFuture<>();
                ret.complete(transaction);
                return ret;
            }

            @Override
            public ProgramResult callConstantFunction(String receiveAddress, ECKey senderPrivateKey, CallTransaction.Function function, Object... funcArgs) {
                Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                        receiveAddress, 0, function, funcArgs);
                tx.sign(senderPrivateKey);
                Block block = sharding.standaloneBlockchain.getBlockchain().getBestBlock();

                Repository repository = sharding.standaloneBlockchain.getBlockchain().getRepository()
                        .getSnapshotTo(block.getStateRoot())
                        .startTracking();

                try {
                    org.ethereum.core.TransactionExecutor executor = new org.ethereum.core.TransactionExecutor
                            (tx, block.getCoinbase(), repository, sharding.standaloneBlockchain.getBlockchain().getBlockStore(),
                                    sharding.standaloneBlockchain.getBlockchain().getProgramInvokeFactory(),
                                    block, new EthereumListenerAdapter(), 0)
                            .withCommonConfig(CommonConfig.getDefault())
                            .setLocalCall(true);

                    executor.init();
                    executor.execute();
                    executor.go();
                    executor.finalization();

                    return executor.getResult();
                } finally {
                    repository.rollback();
                }
            }

            @Override
            public org.ethereum.facade.Repository getRepository() {
                return sharding.standaloneBlockchain.getBlockchain().getRepository();
            }

            @Override
            public void addListener(EthereumListener listener) {
                sharding.standaloneBlockchain.addEthereumListener(listener);
            }

            @Override
            public Blockchain getBlockchain() {
                return sharding.standaloneBlockchain.getBlockchain();
            }
        };

        sharding.validatorConfig = new ValidatorConfig(true,
                Hex.decode("41791102999c339c844880b23950704cc43aa840f3739e365323cda4dfa89e7a"), 0,
                Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826"),
                Hex.decode("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4"));

        sharding.depositContract = new DepositContract(contractConfig.getAddress(),
                contractConfig.getBin(), contractConfig.getAbi());
        sharding.depositContract.setEthereum(ethereum);

        DepositAuthority authority = sharding.depositAuthority = new UnsecuredDepositAuthority(sharding.validatorConfig);

        // deploy registration contract
        standaloneBlockchain.submitTransaction(sharding.depositContract.deployTx(authority));
        standaloneBlockchain.createBlock();

        Randao randao = sharding.randao = new Randao(new HashMapDB<>());
        sharding.validatorService = new ValidatorServiceImpl(ethereum, sharding.validatorConfig,
                sharding.depositContract, authority, randao);

        sharding.validatorRepository = new ValidatorRepositoryImpl(standaloneBlockchain.getBlockchain().getBlockStore(),
                standaloneBlockchain.getBlockchain().getTransactionStore(), sharding.depositContract);

        return sharding;
    }

    public static ValidatorService brandNewValidatorService(ShardingBootstrap bootstrap) {
        return new ValidatorServiceImpl(bootstrap.ethereum,
                bootstrap.validatorConfig, bootstrap.depositContract, bootstrap.depositAuthority, bootstrap.randao);
    }

    public static class ShardingBootstrap {
        public StandaloneBlockchain standaloneBlockchain;
        public ValidatorService validatorService;
        public ValidatorRepository validatorRepository;
        public DepositContract depositContract;
        public ValidatorConfig validatorConfig;
        public DepositAuthority depositAuthority;

        Ethereum ethereum;
        DbFlushManager flushManager;
        Randao randao;
    }
}

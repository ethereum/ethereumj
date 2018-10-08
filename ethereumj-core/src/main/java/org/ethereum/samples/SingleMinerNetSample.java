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
package org.ethereum.samples;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.publish.event.BlockAdded;
import org.ethereum.samples.util.Account;
import org.ethereum.samples.util.TransactionSubmitter;
import org.ethereum.samples.util.Contract;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.nio.file.Files.readAllBytes;
import static org.ethereum.core.Denomination.ETHER;
import static org.ethereum.publish.Subscription.to;
import static org.ethereum.publish.event.Events.Type.BLOCK_ADED;

/**
 * Basic class for independent private network samples.
 * Within this sample single node starts with miner mode.
 * To reduce block's waiting time the mining difficulty setted up to pretty low value (block adding speed is about 1 block per second).
 * This class can be used as a base for free transactions testing
 * (everyone may use that 'usr1pass' sender which has pretty enough fake coins)
 * <p>
 * Created by Eugene Shevchenko on 05.10.2018.
 */
public class SingleMinerNetSample {

    protected abstract static class Config {

        @Autowired
        private ResourceLoader resourceLoader;

        protected Resource loadSampleResource(String path) {
            return resourceLoader.getResource("classpath:samples" + path);
        }

        protected byte[] loadContractSource(String contractName) {
            try {
                Resource resource = loadSampleResource("/contracts/" + contractName);
                return readAllBytes(resource.getFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(contractName + " contract source loading error: ", e);
            }
        }

        public abstract SingleMinerNetSample sample();

        protected Map<String, Object> getExtraConfig() {
            return new LinkedHashMap<String, Object>() {{
                put("sync.enabled", false);
                put("sync.makeDoneByTimeout", 60);
                put("peer.discovery.enabled", false);
                put("peer.listen.port", 0);
                put("peer.privateKey", "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec");
                put("peer.networkId", 555);
                put("mine.start", true);
                put("mine.fullDataSet", false);
                put("mine.extraDataHex", "cccccccccccccccccccc");
                put("mine.minBlockTimeoutMsec", 0);
                put("mine.cpuMineThreads", 1);
                put("genesis", "sample-local-genesis.json");
                put("database.dir", "local-net-sample-db");
                put("cache.flush.blocks", 10);
            }};
        }

        @Bean
        public final SystemProperties systemProperties() {
            SystemProperties props = SystemProperties.getDefault();
            props.setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());

            Map<String, Object> extraConfig = getExtraConfig();
            if (!extraConfig.isEmpty()) {
                props.overrideParams(ConfigFactory.parseMap(extraConfig));
            }

            return props;
        }

        @Bean
        public TransactionSubmitter transactionSubmitter(Ethereum ethereum) {
            return new TransactionSubmitter(ethereum);
        }

        @Bean
        public final Account.Register accountRegister() {
            Account.Register register = Account.newRegister().withFaucet("usr1pass");
            registerAccounts(register);
            return register;
        }

        /**
         * Template method for custom accounts installing.
         * Register own accounts via {@link org.ethereum.samples.util.Account.Register} to get some test ether.
         * @param register
         */
        protected void registerAccounts(Account.Register register) {

        }

        @Bean
        public final Contract.Register contractRegister(SolidityCompiler compiler) {
            Contract.Register register = Contract.newRegister(compiler);
            registerContracts(register);
            return register;
        }

        /**
         * Register your contract via {@link org.ethereum.samples.util.Contract.Register} to deploy it at sample prepare phase.
         * @param register
         */
        protected void registerContracts(Contract.Register register) {

        }
    }

    protected static final Logger logger = LoggerFactory.getLogger("sample");

    @Autowired
    protected Ethereum ethereum;
    @Autowired
    protected SolidityCompiler compiler;
    @Autowired
    protected TransactionSubmitter txSubmitter;
    @Autowired
    protected Account.Register accountRegister;
    @Autowired
    protected Contract.Register contractRegister;

    protected final Account account(String id) {
        return accountRegister.get(id);
    }

    protected final Contract contract(String id) {
        return contractRegister.get(id);
    }

    protected final Contract.Caller contractCaller(String accountId, String contractId) {
        Account caller = account(accountId);
        return contract(contractId).newCaller(caller.getKey(), ethereum, txSubmitter);
    }

    private CompletableFuture<Void> deployContracts() {
        ECKey faucetKey = accountRegister.getFaucet().getKey();

        CompletableFuture[] futures = contractRegister.contracts().stream()
                .filter(contract -> !contract.isDeployed())
                .map(contract -> txSubmitter.deployTransaction(faucetKey, contract.getBinaryCode()).submit()
                        .thenApply(receipt -> contract.deployedAt(receipt.getTransaction().getContractAddress())))
                .toArray(CompletableFuture[]::new);


        return CompletableFuture.allOf(futures).whenComplete((smth, err) -> {
            if (err == null) {
                logger.info("All predefined contracts successfully deployed.");
            } else {
                logger.info("Contract deployment error: ", err);
            }
        });
    }

    private CompletableFuture<Void> transferFundsToAccounts() {
        ECKey faucetKey = accountRegister.getFaucet().getKey();

        CompletableFuture<TransactionReceipt>[] futures = accountRegister.accountsWithoutFaucet().stream()
                .map(account -> txSubmitter
                        .transferTransaction(faucetKey, account.getAddress(), 100, ETHER)
                        .submit())
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).whenComplete((smth, err) -> {
            if (err == null) {
                logger.info("All funds transfers for predefined accounts performed successfully.");
            } else {
                logger.error("Funds transferring error: ", err);
            }
        });
    }

    @PostConstruct
    public final void initSample() {
        this.ethereum
                .subscribe(to(BLOCK_ADED, this::onImportStarted).oneOff())
                .subscribe(to(BLOCK_ADED, this::printHeartbeat));
    }

    private void printHeartbeat(BlockAdded.Data data) {
        if (data.getBlockSummary().getBlock().getNumber() % 15 == 0) {
            logger.info("heartbeat: block #{} mined and imported.", data.getBlockSummary().getBlock().getNumber());
        }
    }

    private void onImportStarted(BlockAdded.Data data) {
        logger.info("Single miner network is up. The first block #{} has been imported.", data.getBlockSummary().getBlock().getNumber());

        List<CompletableFuture> initActions = new ArrayList<>();
        initActions.add(transferFundsToAccounts());
        initActions.add(deployContracts());

        CompletableFuture.allOf(initActions.toArray(new CompletableFuture[]{}))
                .whenComplete((aVoid, err) -> {
                    if (err == null) {
                        logger.info("Sample components successfully deployed.");
                        onSampleReady();
                    } else {
                        logger.error("Sample setup failed with error: ", err);
                        onSampleFailed(err);
                    }
                });
    }

    protected void onSampleReady() {

    }

    protected void onSampleFailed(Throwable err) {
        System.exit(1);
    }

    public static void main(String[] args) {

        class Cfg extends Config {

            @Bean
            @Override
            public SingleMinerNetSample sample() {
                return new SingleMinerNetSample();
            }
        }

        EthereumFactory.createEthereum(Cfg.class);
    }

    static {
        overrideLoggingLevel("mine", Level.WARN);
        overrideLoggingLevel("blockchain", Level.WARN);
        overrideLoggingLevel("net", Level.WARN);
        overrideLoggingLevel("db", Level.WARN);
        overrideLoggingLevel("sync", Level.WARN);
    }

    private static void overrideLoggingLevel(String loggerName, Level level) {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(loggerName);
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(level);
    }

}

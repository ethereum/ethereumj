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
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.db.BlockStore;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.db.RepositoryWrapper;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.sharding.config.DepositContractConfig;
import org.ethereum.sharding.service.ValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mikhail Kalinin
 * @since 26.07.2018
 */
public class ShardingWorldManager extends WorldManager {

    private static final Logger logger = LoggerFactory.getLogger("sharding");

    DepositContractConfig contractConfig;
    DbFlushManager dbFlushManager;

    private CompletableFuture<Void> contractInit = new CompletableFuture<>();

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
            RepositoryWrapper repository = ((RepositoryWrapper) getRepository());
            repository.saveCode(contractConfig.getAddress(), contractConfig.getBin());
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
        contractInit.thenRunAsync(validatorService::init);
    }
}

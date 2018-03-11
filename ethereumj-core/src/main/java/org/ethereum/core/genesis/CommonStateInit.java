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
package org.ethereum.core.genesis;

import org.ethereum.core.Blockchain;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonStateInit implements StateInit {
    private static final Logger logger = LoggerFactory.getLogger("general");
    private Genesis genesis;
    private Repository repository;
    private Blockchain blockchain;
    private Genesis initGenesis;

    public CommonStateInit(Genesis genesis, Repository repository, Blockchain blockchain) {
        this.genesis = genesis;
        this.repository = repository;
        this.blockchain = blockchain;
        init();
    }

    @Override
    public void initDB() {
        if (blockchain.getBlockByNumber(0) != null) {
            return;  // Already initialized
        }
        logger.info("DB is empty - adding Genesis");
        Genesis.populateRepository(repository, genesis);
        repository.commit();
        ((org.ethereum.facade.Blockchain)blockchain).getBlockStore().saveBlock(genesis, genesis.getCumulativeDifficulty(), true);
        blockchain.setBestBlock(genesis);
        blockchain.setTotalDifficulty(genesis.getCumulativeDifficulty());

        logger.info("Genesis block loaded");
    }


    private void init() {
        this.initGenesis = genesis;
    }

    @Override
    public Genesis getInitGenesis() {
        return initGenesis;
    }
}

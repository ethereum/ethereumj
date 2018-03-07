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
package org.ethereum.core.consensus;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Genesis;
import org.ethereum.core.genesis.CommonStateInit;
import org.ethereum.core.genesis.StateInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class PoWConsensusStrategy implements ConsensusStrategy {
    private SystemProperties systemProperties;

    @Autowired
    private BlockchainImpl blockchain;
    private StateInit stateInit;

    public PoWConsensusStrategy() {
    }

    public PoWConsensusStrategy(SystemProperties systemProperties, ApplicationContext ctx) {
       this(systemProperties);
    }

    public PoWConsensusStrategy(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;

        // TODO: Add default blockchainImpl when it's not provided
    }

    @Override
    public void init() {

    }

    @Override
    public StateInit initState(Genesis genesis) {
        if (stateInit != null) {
            throw new RuntimeException("State is already initialized");
        } else {
            this.stateInit = new CommonStateInit(genesis, blockchain.getRepository(), blockchain);
        }
        return stateInit;
    }

    @Override
    public StateInit getInitState() {
        if (stateInit == null) {
            throw new RuntimeException("State is not initialized");
        }
        return stateInit;
    }
}

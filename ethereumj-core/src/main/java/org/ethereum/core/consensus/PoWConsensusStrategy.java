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
import org.ethereum.validator.BlockHashRule;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.DependentBlockHeaderRule;
import org.ethereum.validator.DifficultyRule;
import org.ethereum.validator.ExtraDataRule;
import org.ethereum.validator.GasLimitRule;
import org.ethereum.validator.GasValueRule;
import org.ethereum.validator.ParentBlockHeaderValidator;
import org.ethereum.validator.ParentGasLimitRule;
import org.ethereum.validator.ParentNumberRule;
import org.ethereum.validator.ProofOfWorkRule;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class PoWConsensusStrategy implements ConsensusStrategy {
    private SystemProperties systemProperties;

    private BlockHeaderValidator blockHeaderValidator;
    private ParentBlockHeaderValidator parentBlockHeaderValidator;
    private BlockchainImpl blockchain;
    private StateInit stateInit;

    public PoWConsensusStrategy(SystemProperties systemProperties, ApplicationContext ctx) {
       this(systemProperties);
        blockchain = ctx.getBean(BlockchainImpl.class);
    }

    public PoWConsensusStrategy(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;

        List<BlockHeaderRule> rules = new ArrayList<>(asList(
                new GasValueRule(),
                new ExtraDataRule(systemProperties),
                new ProofOfWorkRule(),
                new GasLimitRule(systemProperties),
                new BlockHashRule(systemProperties)
        ));
        blockHeaderValidator = new BlockHeaderValidator(rules);


        List<DependentBlockHeaderRule> parentRules = new ArrayList<>(asList(
                new ParentNumberRule(),
                new DifficultyRule(systemProperties),
                new ParentGasLimitRule(systemProperties)
        ));
        parentBlockHeaderValidator = new ParentBlockHeaderValidator(parentRules);
        // TODO: Add default blockchainImpl when it's not provided
    }

    @Override
    public BlockHeaderValidator getHeaderValidator() {
        return blockHeaderValidator;
    }

    @Override
    public ParentBlockHeaderValidator getParentHeaderValidator() {
        return parentBlockHeaderValidator;
    }

    @Override
    public BlockchainImpl getBlockchain() {
        return blockchain;
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

    public void setBlockchain(BlockchainImpl blockchain) {
        this.blockchain = blockchain;
        blockchain.setParentHeaderValidator(parentBlockHeaderValidator);
    }
}

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
package org.ethereum.casper.config;

import org.ethereum.config.CommonConfig;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.core.TransactionExecutorFactory;
import org.ethereum.casper.core.CasperBlockchain;
import org.ethereum.casper.core.CasperHybridConsensusStrategy;
import org.ethereum.casper.core.CasperTransactionExecutor;
import org.ethereum.core.consensus.ConsensusStrategy;
import org.ethereum.db.BlockStore;
import org.ethereum.listener.EthereumListener;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class CasperBeanConfig extends CommonConfig {

    @Override
    public ConsensusStrategy consensusStrategy() {
        return new CasperHybridConsensusStrategy(systemProperties(), ctx);
    }

    @Override
    @Bean
    public Blockchain blockchain() {
        return new CasperBlockchain(systemProperties());
    }

    @Override
    @Bean
    public TransactionExecutorFactory transactionExecutorFactory() {
        return new CasperTransactionExecutorFactory();
    }

    class CasperTransactionExecutorFactory implements TransactionExecutorFactory {

        @Override
        public TransactionExecutor createTransactionExecutor(Transaction tx, byte[] coinbase, Repository track,
                                                             BlockStore blockStore, ProgramInvokeFactory programInvokeFactory,
                                                             Block currentBlock) {
            return new CasperTransactionExecutor(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock);
        }

        @Override
        public TransactionExecutor createTransactionExecutor(Transaction tx, byte[] coinbase, Repository track,
                                                             BlockStore blockStore, ProgramInvokeFactory programInvokeFactory,
                                                             Block currentBlock, EthereumListener listener, long gasUsedInTheBlock) {
            return new CasperTransactionExecutor(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock,
                    listener, gasUsedInTheBlock);
        }
    }
}

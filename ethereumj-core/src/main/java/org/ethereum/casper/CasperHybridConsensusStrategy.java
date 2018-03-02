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
package org.ethereum.casper;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.core.casper.CasperBlockchain;
import org.ethereum.core.casper.CasperTransactionExecutor;
import org.ethereum.core.consensus.ConsensusStrategy;
import org.ethereum.core.genesis.CasperStateInit;
import org.ethereum.core.genesis.StateInit;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;
import org.ethereum.casper.service.CasperValidatorService;
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class CasperHybridConsensusStrategy implements ConsensusStrategy {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private SystemProperties systemProperties;

    private CasperBlockchain blockchain;

    private CasperValidatorService casperValidatorService;

    private Ethereum ethereum;

    private ApplicationContext ctx;

    private StateInit stateInit;

    private CallTransaction.Contract casper = null;

    private String casperAddress;  // FIXME: why should we have casper addresses in two places?. It's already in SystemProperties

    public CasperHybridConsensusStrategy() {
    }

    public CasperHybridConsensusStrategy(SystemProperties systemProperties, ApplicationContext ctx) {
        this.systemProperties = systemProperties;
        this.ctx = ctx;
    }

    @Override
    public void init() {
        getCasperValidatorService();
    }

    @Override
    public StateInit initState(Genesis genesis) {
        if (stateInit != null) {
            throw new RuntimeException("State is already initialized");
        } else {
            this.stateInit = new CasperStateInit(genesis, blockchain.getRepository(), blockchain, ctx, systemProperties);
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

    public CallTransaction.Contract getCasper() {
        initCasper();
        return casper;
    }

    private void initCasper() {
        if (casper == null) {
            casperAddress = Hex.toHexString(systemProperties.getCasperAddress());
            String casperAbi = systemProperties.getCasperAbi();
            casper = new CallTransaction.Contract(casperAbi);
        }
    }

    public Object[] constCallCasper(String func, Object... funcArgs) {
        initCasper();
        ProgramResult r = ethereum.callConstantFunction(casperAddress,
                casper.getByName(func), funcArgs);
        return casper.getByName(func).decodeResult(r.getHReturn());
    }


    public Object[] constCallCasper(Block block, String func, Object... funcArgs) {
        initCasper();
        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                casperAddress, 0, casper.getByName(func), funcArgs);
        ProgramResult r = ethereum.callConstantFunction(block, casperAddress,
                casper.getByName(func), funcArgs);
        return casper.getByName(func).decodeResult(r.getHReturn());
    }

    public byte[] getCasperAddress() {
        return Hex.decode(casperAddress);
    }

    public CasperValidatorService getCasperValidatorService() {
        if (casperValidatorService == null &&
                systemProperties.getCasperValidatorEnabled() != null && systemProperties.getCasperValidatorEnabled()) {
            logger.info("Casper validator is enabled, starting...");
            this.casperValidatorService = ctx.getBean(CasperValidatorService.class);
        }

        return casperValidatorService;
    }

    public void setEthereum(Ethereum ethereum) {
        this.ethereum = ethereum;
    }

    @Autowired
    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = (CasperBlockchain) blockchain;
    }
}

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
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutor;
import org.ethereum.core.casper.CasperBlockchain;
import org.ethereum.core.casper.CasperTransactionExecutor;
import org.ethereum.core.genesis.CasperStateInit;
import org.ethereum.core.genesis.StateInit;
import org.ethereum.db.BlockStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.CasperValidatorService;
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
import org.ethereum.vm.program.ProgramResult;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class CasperHybridConsensusStrategy implements ConsensusStrategy {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private SystemProperties systemProperties;

    private BlockHeaderValidator blockHeaderValidator;

    private ParentBlockHeaderValidator parentBlockHeaderValidator;

    private CasperBlockchain blockchain;

    private CasperValidatorService casperValidatorService;

    private Ethereum ethereum;

    private ApplicationContext ctx;

    private StateInit stateInit;

    private CallTransaction.Contract casper = null;

    private String casperAddress;  // FIXME: why should we have casper addresses in two places?. It's already in SystemProperties

    public CasperHybridConsensusStrategy(SystemProperties systemProperties, ApplicationContext ctx) {
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

        blockchain = ctx.getBean(CasperBlockchain.class);
        blockchain.setStrategy(this);
        ethereum = ctx.getBean(Ethereum.class);
        this.ctx = ctx;
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
    public Blockchain getBlockchain() {
        return blockchain;
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

    // FIXME: Magic
    public void setBlockchain(CasperBlockchain blockchain) {
        this.blockchain = blockchain;
        blockchain.setParentHeaderValidator(parentBlockHeaderValidator);
        blockchain.setStrategy(this);
    }

    public void setEthereum(Ethereum ethereum) {
        this.ethereum = ethereum;
    }
}

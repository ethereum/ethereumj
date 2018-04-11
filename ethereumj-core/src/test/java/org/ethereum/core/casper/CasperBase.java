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
package org.ethereum.core.casper;

import org.ethereum.casper.config.CasperBeanConfig;
import org.ethereum.casper.config.CasperProperties;
import org.ethereum.casper.config.net.CasperTestConfig;
import org.ethereum.casper.core.CasperBlockchain;
import org.ethereum.casper.core.CasperFacade;
import org.ethereum.casper.core.CasperPendingStateImpl;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.casper.config.net.CasperTestNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.core.Genesis;
import org.ethereum.core.Repository;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.datasource.CountingBytesSource;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.PruneManager;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.RepositoryWrapper;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.casper.validator.NullSenderTxValidator;
import org.ethereum.vm.program.ProgramPrecompile;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

// We have all mocks here and not all of them are used in every test, so strict stubs should be turned off
@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class CasperBase {
    @Mock
    ApplicationContext context;

    @Spy
    final CasperProperties systemProperties = new CasperProperties();

    @InjectMocks
    private CommonConfig commonConfig = new CasperBeanConfig() {
        @Override
        public Source<byte[], ProgramPrecompile> precompileSource() {
            return null;
        }

        @Override
        public SystemProperties systemProperties() {
            return systemProperties;
        }
    };

    CasperBlockchain blockchain;

    protected CompositeEthereumListener defaultListener = new CompositeEthereumListener();

    @InjectMocks
    EthereumImpl ethereum = new EthereumImpl(systemProperties, defaultListener);

    @InjectMocks
    CasperFacade casper = new CasperFacade();

    Repository repository = new RepositoryWrapper();

    @InjectMocks
    CasperPendingStateImpl casperPendingState = new CasperPendingStateImpl(defaultListener);

    StandaloneBlockchain bc;

    WorldManager worldManager;

    @Before
    public void setup() throws Exception {
        // Just trust me!
        // FIXME: Make it a little bit readable

        BlockchainNetConfig config = config();
        ((CasperTestConfig) config.getConfigForBlock(0)).addNullSenderTxValidators(new NullSenderTxValidator(casper::isVote));
        systemProperties.setBlockchainConfig(config);
        Resource casperGenesis = new ClassPathResource("/genesis/casper.json");
        systemProperties.useGenesis(casperGenesis.getInputStream());
        systemProperties.overrideParams(
                "casper.contractBin", "/casper/casper.bin",
                "casper.contractAbi", "/casper/casper.abi"
                );

        MockitoAnnotations.initMocks(this);

        this.ethereum.setCommonConfig(commonConfig);
        this.worldManager = Mockito.mock(WorldManager.class);

        this.bc = new StandaloneBlockchain() {
            @Override
            public BlockchainImpl getBlockchain() {
                if (blockchain == null) {
                    blockchain = createBlockchain();
                    addEthereumListener(new EthereumListenerAdapter() {
                        @Override
                        public void onBlock(BlockSummary blockSummary) {
                            lastSummary = blockSummary;
                        }
                    });
                }
                return blockchain;
            }

            private BlockchainImpl createBlockchain() {
                SystemProperties.getDefault().setBlockchainConfig(systemProperties.getBlockchainConfig());

                IndexedBlockStore blockStore = new IndexedBlockStore();
                blockStore.init(new HashMapDB<byte[]>(), new HashMapDB<byte[]>());

                stateDS = new HashMapDB<>();
                pruningStateDS = new JournalSource<>(new CountingBytesSource(stateDS));
                pruneManager = new PruneManager(blockStore, pruningStateDS,
                        stateDS, SystemProperties.getDefault().databasePruneDepth());

                final RepositoryRoot repository = new RepositoryRoot(pruningStateDS);

                ProgramInvokeFactoryImpl programInvokeFactory = new ProgramInvokeFactoryImpl();
                listener = defaultListener;

                BlockchainImpl blockchain = new CasperBlockchain(systemProperties).withEthereumListener(listener)
                        .withAdminInfo(new AdminInfo())
                        .withEventDispatchThread(new EventDispatchThread())
                        .withTransactionStore(new TransactionStore(new HashMapDB()))
                        .withCommonConfig(commonConfig)
                        .withBlockStore(blockStore);
                blockchain.setRepository(repository);
                blockchain.setParentHeaderValidator(new DependentBlockHeaderRuleAdapter());
                blockchain.setProgramInvokeFactory(programInvokeFactory);
                blockchain.setPruneManager(pruneManager);
                ((CasperBlockchain) blockchain).setFinalizedBlocks(new HashMapDB<>());

                blockchain.byTest = true;

                pendingState = casperPendingState;
                pendingState.setCommonConfig(commonConfig);

                pendingState.setBlockchain(blockchain);
                blockchain.setPendingState(pendingState);
                return blockchain;
            }
        }.withNetConfig(systemProperties.getBlockchainConfig());

        this.blockchain = (CasperBlockchain) bc.getBlockchain();
        casper.setEthereum(ethereum);
        blockchain.setCasper(casper);
        Mockito.when(context.getBean(CasperBlockchain.class)).thenReturn(blockchain);
        Mockito.when(worldManager.getBlockchain()).thenReturn(blockchain);
        Mockito.when(worldManager.getBlockStore()).thenReturn(blockchain.getBlockStore());
        ((RepositoryWrapper) repository).setBlockchain(bc.getBlockchain());
        Mockito.when(worldManager.getRepository()).thenReturn(repository);
        doAnswer((Answer<Void>) invocation -> {
            Object arg0 = invocation.getArgument(0);
            defaultListener.addListener((EthereumListener) arg0);
            return null;
        }).when(worldManager).addListener(any(EthereumListener.class));
        ethereum.setWorldManager(worldManager);
        ethereum.setProgramInvokeFactory(new ProgramInvokeFactoryImpl());
        ethereum.setPendingState(blockchain.getPendingState());
        ethereum.setChannelManager(Mockito.mock(ChannelManager.class));

        // Push pending txs in StandaloneBlockchain
        ethereum.addListener(new EthereumListenerAdapter(){
            @Override
            public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
                if (state.equals(PendingTransactionState.NEW_PENDING)) {
                    bc.submitTransaction(txReceipt.getTransaction());
                }
            }
        });
    }

    /**
     * Same logic as in WorldManager.loadBlockchain
     */
    protected void loadBlockchain() {

        Genesis genesis = Genesis.getInstance(systemProperties);
        Genesis.populateRepository(repository, genesis);

//            repository.commitBlock(genesis.getHeader());
        repository.commit();

        blockchain.getBlockStore().saveBlock(Genesis.getInstance(systemProperties), Genesis.getInstance(systemProperties).getCumulativeDifficulty(), true);

        blockchain.setBestBlock(Genesis.getInstance(systemProperties));
        blockchain.setTotalDifficulty(Genesis.getInstance(systemProperties).getCumulativeDifficulty());

        defaultListener.onBlock(new BlockSummary(Genesis.getInstance(systemProperties), new HashMap<byte[], BigInteger>(), new ArrayList<TransactionReceipt>(), new ArrayList<TransactionExecutionSummary>()));
//            repository.dumpState(Genesis.getInstance(config), 0, 0, null);
    }

    BlockchainNetConfig config() {
        return new CasperTestNetConfig();
    }
}

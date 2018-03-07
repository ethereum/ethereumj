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
import org.ethereum.casper.core.CasperBlockchain;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.casper.config.net.CasperTestNetConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.TransactionExecutorFactory;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.casper.core.CasperHybridConsensusStrategy;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

// We have all mocks here and not all of them are used in every test, so strict stubs should be turned off
@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class CasperBase {
    @Mock
    ApplicationContext context;

    @InjectMocks
    private CommonConfig commonConfig = new CasperBeanConfig() {
        @Override
        public Source<byte[], ProgramPrecompile> precompileSource() {
            return null;
        }
    };

    final SystemProperties systemProperties = commonConfig.systemProperties();

    @Spy
    private TransactionExecutorFactory transactionExecutorFactory = commonConfig.transactionExecutorFactory();

    CasperBlockchain blockchain;

    WorldManager worldManager;

    private CompositeEthereumListener defaultListener = new CompositeEthereumListener();

    @InjectMocks
    EthereumImpl ethereum = new EthereumImpl(systemProperties, defaultListener);

    CasperHybridConsensusStrategy strategy;

    StandaloneBlockchain bc;

    @Before
    public void setup() throws Exception {
        // Just trust me!
        // FIXME: Make it a little bit readable

        systemProperties.setBlockchainConfig(config());
        Resource casperGenesis = new ClassPathResource("/genesis/casper.json");
        systemProperties.useGenesis(casperGenesis.getInputStream());
        systemProperties.overrideParams(
                "consensus.casper.epochLength", "50",
                "consensus.casper.contractBin", "/casper/casper.bin",
                "consensus.casper.contractAbi", "/casper/casper.abi"
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
                pruneManager = new PruneManager(blockStore, pruningStateDS, SystemProperties.getDefault().databasePruneDepth());

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
                ((CasperBlockchain)blockchain).setStrategy(strategy);

                blockchain.byTest = true;

                pendingState = new PendingStateImpl(listener);
                pendingState.setCommonConfig(commonConfig);

                pendingState.setBlockchain(blockchain);
                blockchain.setPendingState(pendingState);
                return blockchain;
            }
        }.withNetConfig(systemProperties.getBlockchainConfig());

        this.blockchain = (CasperBlockchain) bc.getBlockchain();
        Mockito.when(context.getBean(CasperBlockchain.class)).thenReturn(blockchain);
        Mockito.when(worldManager.getBlockchain()).thenReturn(blockchain);
        Mockito.when(worldManager.getBlockStore()).thenReturn(blockchain.getBlockStore());
        RepositoryWrapper wrapper = new RepositoryWrapper();
        wrapper.setBlockchain(bc.getBlockchain());
        Mockito.when(worldManager.getRepository()).thenReturn(wrapper);
        doAnswer((Answer<Void>) invocation -> {
            Object arg0 = invocation.getArgument(0);
            defaultListener.addListener((EthereumListener) arg0);
            return null;
        }).when(worldManager).addListener(any(EthereumListener.class));
        ethereum.setWorldManager(worldManager);
        ethereum.setProgramInvokeFactory(new ProgramInvokeFactoryImpl());
        ethereum.setPendingState(blockchain.getPendingState());
        ethereum.setChannelManager(Mockito.mock(ChannelManager.class));
        this.strategy = (CasperHybridConsensusStrategy) commonConfig.consensusStrategy();
        strategy.setEthereum(ethereum);
        strategy.setBlockchain(blockchain);
        blockchain.setStrategy(strategy);

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

    BlockchainNetConfig config() {
        return new CasperTestNetConfig();
    }
}

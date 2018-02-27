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

import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.net.CasperTestNetConfig;
import org.ethereum.core.BlockSummary;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.EventDispatchThread;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.consensus.CasperHybridConsensusStrategy;
import org.ethereum.core.consensus.ConsensusStrategy;
import org.ethereum.datasource.CountingBytesSource;
import org.ethereum.datasource.JournalSource;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.PruneManager;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.ethereum.validator.DependentBlockHeaderRuleAdapter;
import org.ethereum.vm.program.ProgramPrecompile;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class CasperBase {
    @Mock
    ApplicationContext context;

    final SystemProperties systemProperties = SystemProperties.getDefault();

    private CommonConfig commonConfig;

    CasperBlockchain blockchain;

    private WorldManager worldManager;

    private EthereumImpl ethereum;

    private CompositeEthereumListener defaultListener = new CompositeEthereumListener();

    CasperHybridConsensusStrategy strategy;

    StandaloneBlockchain bc;

    @Before
    public void setup() {
        // Just trust me!
        // FIXME: Make it a little bit readable
        systemProperties.setBlockchainConfig(config());
        systemProperties.setGenesisInfo("casper.json");
        systemProperties.overrideParams(
                "consensus.casper.epochLength", "50",
                "consensus.casper.contractBin", "/casper/casper.bin",
                "consensus.casper.contractAbi", "/casper/casper.abi"
                );
        MockitoAnnotations.initMocks(this);
        this.commonConfig = new CommonConfig() {
            @Override
            public Source<byte[], ProgramPrecompile> precompileSource() {
                return null;
            }

            @Override
            public ConsensusStrategy consensusStrategy() {
                if (strategy == null) {
                    strategy = new CasperHybridConsensusStrategy(systemProperties, context);
                }
                return strategy;
            }
        };

        this.ethereum = new EthereumImpl(systemProperties, defaultListener);
        ethereum.setCommonConfig(commonConfig);
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

                pendingState = new PendingStateImpl(listener, blockchain);

                pendingState.setBlockchain(blockchain);
                blockchain.setPendingState(pendingState);
                return blockchain;
            }
        }.withNetConfig(systemProperties.getBlockchainConfig());

        this.blockchain = (CasperBlockchain) bc.getBlockchain();
        Mockito.when(context.getBean(CasperBlockchain.class)).thenReturn(blockchain);
        Mockito.when(worldManager.getBlockchain()).thenReturn(blockchain);
        Mockito.when(worldManager.getRepository()).thenReturn(bc.getBlockchain().getRepository());
        Mockito.when(worldManager.getBlockStore()).thenReturn(bc.getBlockchain().getBlockStore());
        ethereum.setWorldManager(worldManager);
        ethereum.setProgramInvokeFactory(new ProgramInvokeFactoryImpl());
        ((CasperHybridConsensusStrategy) commonConfig.consensusStrategy()).setEthereum(ethereum);
    }

    BlockchainNetConfig config() {
        return new CasperTestNetConfig();
    }
}

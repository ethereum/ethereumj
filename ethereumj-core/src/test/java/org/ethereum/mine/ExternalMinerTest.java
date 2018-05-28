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
package org.ethereum.mine;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.ImportResult;
import org.ethereum.db.PruneManager;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.LocalBlockchain;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigInteger;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


/**
 * Creates an instance
 */
public class ExternalMinerTest {

    private StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(false);

    private CompositeEthereumListener listener = new CompositeEthereumListener();

    @Mock
    private EthereumImpl ethereum;

    @InjectMocks
    @Resource
    private BlockMiner blockMiner = new BlockMiner(SystemProperties.getDefault(), listener, bc.getBlockchain(),
            bc.getBlockchain().getBlockStore(), bc.getPendingState());;

    @Before
    public void setup() {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        // Initialize mocks created above
        MockitoAnnotations.initMocks(this);

        when(ethereum.addNewMinedBlock(any(Block.class))).thenAnswer(new Answer<ImportResult>() {
            @Override
            public ImportResult answer(InvocationOnMock invocation) throws Throwable {
                Block block = (Block) invocation.getArguments()[0];
                return bc.getBlockchain().tryToConnect(block);
            }
        });
    }

    @Test
    public void externalMiner_shouldWork() throws Exception {

        final Block startBestBlock = bc.getBlockchain().getBestBlock();

        final SettableFuture<MinerIfc.MiningResult> futureBlock = SettableFuture.create();

        blockMiner.setExternalMiner(new MinerIfc() {
            @Override
            public ListenableFuture<MiningResult> mine(Block block) {
//                System.out.print("Mining requested");
                return futureBlock;
            }

            @Override
            public boolean validate(BlockHeader blockHeader) {
                return true;
            }
        });
        Block b = bc.getBlockchain().createNewBlock(startBestBlock, EMPTY_LIST, EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get();
        futureBlock.set(new MinerIfc.MiningResult(ByteUtil.byteArrayToLong(b.getNonce()), b.getMixHash(), b));

        assertThat(bc.getBlockchain().getBestBlock().getNumber(), is(startBestBlock.getNumber() + 1));
    }
}

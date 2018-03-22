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
import org.ethereum.core.Blockchain;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.EMPTY_LIST;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


/**
 * Testing following case:
 * Time limit for block is set
 * When block is mined and time limit is not over, new tx is pushed in pending state,
 * so the new block is mined and old one should be cancelled
 */
@Ignore
public class CancelMiningTest {

    private static final long MIN_BLOCK_TIME = 1000;

    static {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));
        SystemProperties.getDefault().overrideParams("mine.minBlockTimeoutMsec", String.valueOf(MIN_BLOCK_TIME));
    }


    private StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(false);

    private AtomicInteger blocksImported = new AtomicInteger(0);

    private Map<Integer, SettableFuture<MinerIfc.MiningResult>> miningFutures = new HashMap<>();

    @Mock
    private EthereumImpl ethereum;

    Blockchain blockchain = bc.getBlockchain();  // Just to init blockchain in StandaloneBlockchain

    @InjectMocks
    @Resource
    private BlockMiner blockMiner = new BlockMiner(SystemProperties.getDefault(), bc.getListener(), blockchain,
            bc.getPendingState());;

    @Before
    public void setup() {

        // Initialize mocks created above
        MockitoAnnotations.initMocks(this);

        when(ethereum.addNewMinedBlock(any(Block.class))).thenAnswer(new Answer<ImportResult>() {
            @Override
            public ImportResult answer(InvocationOnMock invocation) throws Throwable {
                Block block = (Block) invocation.getArguments()[0];
                blocksImported.incrementAndGet();
                return bc.getBlockchain().tryToConnect(block);
            }
        });
    }

    @Test
    public void onlyOneBlockShouldBeMined() throws Exception {

        blockMiner.setExternalMiner(new MinerIfc() {
            @Override
            public ListenableFuture<MiningResult> mine(Block block) {
                final SettableFuture<MinerIfc.MiningResult> futureBlock = SettableFuture.create();
                miningFutures.put(miningFutures.keySet().size() + 1, futureBlock);
                return futureBlock;
            }

            @Override
            public boolean validate(BlockHeader blockHeader) {
                return true;
            }
        });

        Block block = bc.createBlock();
        assertEquals(1, block.getNumber());


        // Dummy to set last block time in Block miner
        Block b = bc.getBlockchain().createNewBlock(bc.getBlockchain().getBestBlock(), EMPTY_LIST, EMPTY_LIST);
        Ethash.getForBlock(SystemProperties.getDefault(), b.getNumber()).mineLight(b).get();
        // Run it in blocking way to finish
        miningFutures.get(miningFutures.size()).set(new MinerIfc.MiningResult(ByteUtil.byteArrayToLong(b.getNonce()), b.getMixHash(), b));

        for (int i = 0; i < 50; ++i) {
            // This block we will cancel with tx
            Block b2 = bc.getBlockchain().createNewBlock(bc.getBlockchain().getBestBlock(), EMPTY_LIST, EMPTY_LIST);
            Ethash.getForBlock(SystemProperties.getDefault(), b2.getNumber()).mineLight(b2).get();
            // Run it non-blocking to fire new tx until task is finished
            Executors.newSingleThreadExecutor().submit(() -> {
                miningFutures.get(miningFutures.size()).set(new MinerIfc.MiningResult(ByteUtil.byteArrayToLong(b2.getNonce()), b2.getMixHash(), b2));
            });

            ECKey alice = new ECKey();
            Transaction tx = bc.createTransaction(i, alice.getAddress(), 1000000, new byte[0]);
            bc.getPendingState().addPendingTransaction(tx);

            Block b3 = bc.getBlockchain().createNewBlock(bc.getBlockchain().getBestBlock(), new ArrayList<Transaction>() {{
                add(tx);
            }}, EMPTY_LIST);

            miningFutures.get(miningFutures.size()).set(new MinerIfc.MiningResult(ByteUtil.byteArrayToLong(b3.getNonce()), b3.getMixHash(), b3));

            assertEquals(i + 3, bc.getBlockchain().getBestBlock().getNumber()); // + bc.createBlock()
            assertEquals(i + 2, blocksImported.get()); // bc.createBlock() is not counted
            assertEquals(1, bc.getBlockchain().getBestBlock().getTransactionsList().size());
        }
    }
}

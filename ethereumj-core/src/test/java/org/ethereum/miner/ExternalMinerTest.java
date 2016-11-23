package org.ethereum.miner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.FrontierConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.ImportResult;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.mine.BlockMiner;
import org.ethereum.mine.Ethash;
import org.ethereum.mine.MinerIfc;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Creates an instance
 */
public class ExternalMinerTest {

    @Before
    public void setup() {

    }

    @Test
    public void externalMiner_shouldWork() throws Exception {
        SystemProperties.getDefault().setBlockchainConfig(new FrontierConfig(new FrontierConfig.FrontierConstants() {
            @Override
            public BigInteger getMINIMUM_DIFFICULTY() {
                return BigInteger.ONE;
            }
        }));

        final StandaloneBlockchain bc = new StandaloneBlockchain().withAutoblock(false);

        final CompositeEthereumListener listener = new CompositeEthereumListener();
        final BlockMiner blockMiner = new BlockMiner(SystemProperties.getDefault(), listener);
        blockMiner.blockchain = bc.getBlockchain();
        blockMiner.blockStore = bc.getBlockchain().getBlockStore();
        blockMiner.pendingState = bc.getPendingState();
        blockMiner.ethereum = new EthereumImpl(SystemProperties.getDefault(), listener) {
            public ImportResult addNewMinedBlock(Block block) {
                return bc.getBlockchain().tryToConnect(block);
            }
        };

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

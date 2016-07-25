package org.ethereum.core;

import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Anton Nashatyrev on 24.06.2016.
 */
public class CloseTest {

    @Ignore
    @Test
    public void relaunchTest() throws InterruptedException {

        while (true) {
            Ethereum ethereum = EthereumFactory.createEthereum();
            Block bestBlock = ethereum.getBlockchain().getBestBlock();
            Assert.assertNotNull(bestBlock);
            final CountDownLatch latch = new CountDownLatch(1);
            ethereum.addListener(new EthereumListenerAdapter() {
                int counter = 0;
                @Override
                public void onBlock(Block block, List<TransactionReceipt> receipts) {
                    counter++;
                    if (counter > 1100) latch.countDown();
                }
            });
            System.out.println("### Waiting for some blocks to be imported...");
            latch.await();
            System.out.println("### Closing Ethereum instance");
            ethereum.close();
            Thread.sleep(5000);
            System.out.println("### Closed.");
        }
    }
}

package org.ethereum.net.eth.handler;

import org.ethereum.core.BlockIdentifier;
import org.ethereum.net.eth.message.v62.NewBlockHashesMessage;
import org.ethereum.net.server.Channel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Testing {@link Eth62#processNewBlockHashes(NewBlockHashesMessage)}
 */
public class ProcessNewBlockHashesTest {
    private static final Logger logger = LoggerFactory.getLogger("test");

    private class Eth62Tester extends Eth62 {

        private byte[] blockHash;
        private int maxBlockAsk;
        private int skip;
        private boolean reverse;

        private boolean wasCalled = false;

        Eth62Tester() {
            this.syncDone = true;
            this.channel = new Channel();
        }

        void setGetNewBlockHeadersParams(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse) {
            this.blockHash = blockHash;
            this.maxBlockAsk = maxBlocksAsk;
            this.skip = skip;
            this.reverse = reverse;
            this.wasCalled = false;
        }

        @Override
        protected synchronized void sendGetNewBlockHeaders(byte[] blockHash, int maxBlocksAsk, int skip, boolean reverse) {
            this.wasCalled = true;
            logger.error("Request for sending new headers: hash {}, max {}, skip {}, reverse {}",
                    Hex.toHexString(blockHash), maxBlocksAsk, skip, reverse);
            assert Arrays.equals(blockHash, this.blockHash) &&
                    maxBlocksAsk == this.maxBlockAsk && skip == this.skip && reverse == this.reverse;
        }
    }

    private Eth62Tester ethHandler;

    public ProcessNewBlockHashesTest() {
        ethHandler = new Eth62Tester();
    }

    @Test
    public void testSingleHashHandling() {
        List<BlockIdentifier> blockIdentifiers = new ArrayList<>();
        byte[] blockHash = new byte[] {2, 3, 4};
        long blockNumber = 123;
        blockIdentifiers.add(new BlockIdentifier(blockHash, blockNumber));
        NewBlockHashesMessage msg = new NewBlockHashesMessage(blockIdentifiers);

        ethHandler.setGetNewBlockHeadersParams(blockHash, 1, 0, false);
        ethHandler.processNewBlockHashes(msg);
        assert ethHandler.wasCalled;
    }

    @Test
    public void testSeveralHashesHandling() {
        List<BlockIdentifier> blockIdentifiers = new ArrayList<>();
        byte[] blockHash1 = new byte[] {2, 3, 4};
        long blockNumber1 = 123;
        byte[] blockHash2 = new byte[] {5, 3, 4};
        long blockNumber2 = 124;
        byte[] blockHash3 = new byte[] {2, 6, 4};
        long blockNumber3 = 125;
        blockIdentifiers.add(new BlockIdentifier(blockHash1, blockNumber1));
        blockIdentifiers.add(new BlockIdentifier(blockHash2, blockNumber2));
        blockIdentifiers.add(new BlockIdentifier(blockHash3, blockNumber3));
        NewBlockHashesMessage msg = new NewBlockHashesMessage(blockIdentifiers);

        ethHandler.setGetNewBlockHeadersParams(blockHash1, 3, 0, false);
        ethHandler.processNewBlockHashes(msg);
        assert ethHandler.wasCalled;
    }

    @Test
    public void testSeveralHashesMixedOrderHandling() {
        List<BlockIdentifier> blockIdentifiers = new ArrayList<>();
        byte[] blockHash1 = new byte[] {5, 3, 4};
        long blockNumber1 = 124;
        byte[] blockHash2 = new byte[] {2, 3, 4};
        long blockNumber2 = 123;
        byte[] blockHash3 = new byte[] {2, 6, 4};
        long blockNumber3 = 125;
        blockIdentifiers.add(new BlockIdentifier(blockHash1, blockNumber1));
        blockIdentifiers.add(new BlockIdentifier(blockHash2, blockNumber2));
        blockIdentifiers.add(new BlockIdentifier(blockHash3, blockNumber3));
        NewBlockHashesMessage msg = new NewBlockHashesMessage(blockIdentifiers);

        ethHandler.setGetNewBlockHeadersParams(blockHash2, 3, 0, false);
        ethHandler.processNewBlockHashes(msg);
        assert ethHandler.wasCalled;
    }
}

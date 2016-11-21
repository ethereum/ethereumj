package org.ethereum.samples;

import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * With this simple example you can send transaction from address to address in live public network
 * To make it work you just need to set sender's private key and receiver's address
 *
 * Created by Alexander Samtsov on 12.08.16.
 */
public class SendTransaction extends BasicSample {

    private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<ByteArrayWrapper, TransactionReceipt>());

    @Override
    public void onSyncDone() throws Exception {
        ethereum.addListener(new EthereumListenerAdapter() {
            // when block arrives look for our included transactions
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                SendTransaction.this.onBlock(block, receipts);
            }
        });


        String toAddress = "";
        logger.info("Sending transaction to net and waiting for inclusion");
        sendTxAndWait(Hex.decode(toAddress), new byte[0]);
        logger.info("Transaction included!");}


    private void onBlock(Block block, List<TransactionReceipt> receipts) {
        for (TransactionReceipt receipt : receipts) {
            ByteArrayWrapper txHashW = new ByteArrayWrapper(receipt.getTransaction().getHash());
            if (txWaiters.containsKey(txHashW)) {
                txWaiters.put(txHashW, receipt);
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }


    private TransactionReceipt sendTxAndWait(byte[] receiveAddress, byte[] data) throws InterruptedException {

        byte[] senderPrivateKey = Hex.decode("");
        byte[] fromAddress = ECKey.fromPrivate(senderPrivateKey).getAddress();
        BigInteger nonce = ethereum.getRepository().getNonce(fromAddress);
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                ByteUtil.longToBytesNoLeadZeroes(200000),
                receiveAddress,
                ByteUtil.bigIntegerToBytes(BigInteger.valueOf(1)),  // 1_000_000_000 gwei, 1_000_000_000_000L szabo, 1_000_000_000_000_000L finney, 1_000_000_000_000_000_000L ether
                data);

        tx.sign(ECKey.fromPrivate(senderPrivateKey));
        logger.info("<=== Sending transaction: " + tx);
        ethereum.submitTransaction(tx);

        return waitForTx(tx.getHash());
    }


    private TransactionReceipt waitForTx(byte[] txHash) throws InterruptedException {
        ByteArrayWrapper txHashW = new ByteArrayWrapper(txHash);
        txWaiters.put(txHashW, null);
        long startBlock = ethereum.getBlockchain().getBestBlock().getNumber();

        while(true) {
            TransactionReceipt receipt = txWaiters.get(txHashW);
            if (receipt != null) {
                return receipt;
            } else {
                long curBlock = ethereum.getBlockchain().getBestBlock().getNumber();
                if (curBlock > startBlock + 16) {
                    throw new RuntimeException("The transaction was not included during last 16 blocks: " + txHashW.toString().substring(0,8));
                } else {
                    logger.info("Waiting for block with transaction 0x" + txHashW.toString().substring(0,8) +
                            " included (" + (curBlock - startBlock) + " blocks received so far) ...");

                }
            }
            synchronized (this) {
                wait(20000);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        class Config extends BasicSample{
            @Bean
            public BasicSample sampleBean() {
                return new SendTransaction();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);

    }

}

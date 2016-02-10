package org.ethereum.samples;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anton Nashatyrev on 05.02.2016.
 */
public class PendingStateSample extends BasicSample {

    private Map<ByteArrayWrapper, Transaction> pendingTxs = new HashMap<>();

    @Override
    public void onSyncDone() {
        ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                for (Transaction tx : transactions) {
                    PendingStateSample.this.onPendingTransactionReceived(tx);
                }
            }

            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                PendingStateSample.this.onBlock(block, receipts);
            }
        });
    }

    void onPendingTransactionReceived(Transaction tx) {
        if (tx.isValueTx()) {
            byte[] sender = tx.getSender();
            byte[] receiver = tx.getReceiveAddress();
            BigInteger value = ByteUtil.bytesToBigInteger(tx.getValue());
            BigInteger senderBalance = ethereum.getRepository().getBalance(sender);
            BigInteger receiverBalance = ethereum.getRepository().getBalance(receiver);
            BigInteger senderBalancePending = ethereum.getPendingState().getBalance(sender);
            BigInteger receiverBalancePending = ethereum.getPendingState().getBalance(receiver);
            String s = "         " + Hex.toHexString(sender) + " --- (" + value + ") ---> " + Hex.toHexString(receiver) + "\n";
            s += "Current: " + senderBalance + "\t\t\t" + receiverBalance + "\n";
            s += "Pending: " + senderBalancePending + "\t\t\t" + receiverBalancePending;
            logger.info(" - New pending transaction 0x" + Hex.toHexString(tx.getHash()).substring(0, 8) + ": \n" + s);

            pendingTxs.put(new ByteArrayWrapper(tx.getHash()), tx);
        }
    }

    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        for (Transaction tx : block.getTransactionsList()) {
            ByteArrayWrapper txHash = new ByteArrayWrapper(tx.getHash());
            Transaction ptx = pendingTxs.get(txHash);
            if (ptx != null) {
                byte[] sender = tx.getSender();
                byte[] receiver = tx.getReceiveAddress();
                BigInteger value = ByteUtil.bytesToBigInteger(tx.getValue());
                BigInteger senderBalance = ethereum.getRepository().getBalance(sender);
                BigInteger receiverBalance = ethereum.getRepository().getBalance(receiver);
                String s = "         " + Hex.toHexString(sender) + " --- (" + value + ") ---> " + Hex.toHexString(receiver) + "\n";
                s += "Current: " + senderBalance + "\t\t\t" + receiverBalance + "\n";
                logger.info(" + Pending transaction cleared 0x" + Hex.toHexString(tx.getHash()).substring(0, 8) +
                        " in block " + block.getShortDescr() + ": \n" + s);

                pendingTxs.remove(txHash);
            }
        }
    }

    private static class Config {
        @Bean
        public PendingStateSample pendingStateSample() {
            return new PendingStateSample();
        }
    }


    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}

package org.ethereum.samples.util;

import org.ethereum.core.Block;
import org.ethereum.core.Denomination;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Ethereum;
import org.ethereum.publish.event.BlockAdded;
import org.ethereum.publish.event.PendingTransactionUpdated;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.ethereum.publish.event.Events.Type.BLOCK_ADED;
import static org.ethereum.publish.event.Events.Type.PENDING_TRANSACTION_UPDATED;
import static org.ethereum.util.BIUtil.toBI;

public class TransactionSubmitter {

    private final Ethereum ethereum;
    private final Map<ByteArrayWrapper, TransactionBuilder> activeByTxHash = new ConcurrentHashMap<>();
    private final Map<ByteArrayWrapper, Queue<TransactionBuilder>> txQueueBySenderAddr = new ConcurrentHashMap<>();

    public TransactionSubmitter(Ethereum ethereum) {
        this.ethereum = ethereum
                .subscribe(PENDING_TRANSACTION_UPDATED, this::onPendingTransactionUpdated)
                .subscribe(BLOCK_ADED, this::onBlockAdded);
    }

    public TransactionBuilder newTransaction(ECKey senderKey, byte[] receiverAddress) {
        return new TransactionBuilder(senderKey, receiverAddress);
    }

    public TransactionBuilder transferTransaction(ECKey senderKey, byte[] receiverAddress, long value, Denomination denomination) {
        return newTransaction(senderKey, receiverAddress)
                .value(toBI(value), denomination);
    }

    public TransactionBuilder deployTransaction(ECKey senderKey, byte[] binaryCode) {
        return newTransaction(senderKey, ByteUtil.EMPTY_BYTE_ARRAY)
                .data(binaryCode);
    }

    public TransactionBuilder invokeTransaction(ECKey senderKey, byte[] contractAddress, byte[] encodedInvocationData) {
        return newTransaction(senderKey, contractAddress)
                .data(encodedInvocationData);
    }

    private void onPendingTransactionUpdated(PendingTransactionUpdated.Data data) {
        TransactionReceipt receipt = data.getReceipt();
        if (receipt.isSuccessful()) return;

        byte[] txHash = receipt.getTransaction().getHash();
        TransactionBuilder tb = activeByTxHash.get(wrap(txHash));
        if (tb != null && tb.isApplicable(receipt)) {
            tb.completeExceptionally(receipt.getError());
        }
    }

    private void onBlockAdded(BlockAdded.Data data) {
        Block block = data.getBlockSummary().getBlock();
        List<TransactionReceipt> receipts = data.getBlockSummary().getReceipts();

        Map<ByteArrayWrapper, TransactionReceipt> receiptByTxHash = receipts.stream()
                .filter(receipt -> activeByTxHash.containsKey(wrap(receipt.getTransaction().getHash())))
                .collect(toMap(receipt -> new ByteArrayWrapper(receipt.getTransaction().getHash()), identity()));


        activeByTxHash.forEach((txHash, tb) -> {
            TransactionReceipt receipt = receiptByTxHash.get(txHash);
            if (receipt != null) {
                if (receipt.isSuccessful()) {
                    tb.complete(receipt);
                } else {
                    tb.completeExceptionally(receipt.getError());
                }
            } else if (tb.isExpired(block)) {
                tb.completeExceptionally("The transaction was not included during last " + tb.waitBlocksCount + " blocks.");
            }
        });
    }


    private void activate(TransactionBuilder txBuilder) {
        txBuilder.buildAndSubmit();
        activeByTxHash.put(wrap(txBuilder.txHash), txBuilder);
    }

    private void addToSubmitQueue(TransactionBuilder txBuilder) {
        ByteArrayWrapper address = wrap(txBuilder.senderKey.getAddress());
        Queue<TransactionBuilder> queue = txQueueBySenderAddr.computeIfAbsent(address, addr -> new LinkedList<>());
        synchronized (queue) {
            if (queue.isEmpty()) {
                activate(txBuilder);
            }
            queue.add(txBuilder);
        }
    }


    private void removeFromSubmitQueue(TransactionBuilder txBuilder) {
        ByteArrayWrapper address = new ByteArrayWrapper(txBuilder.senderKey.getAddress());
        Queue<TransactionBuilder> queue = txQueueBySenderAddr.get(address);
        synchronized (queue) {
            queue.poll();
            activeByTxHash.remove(wrap(txBuilder.txHash));
            if (queue.isEmpty()) {
                txQueueBySenderAddr.remove(address);
            } else {
                activate(queue.peek());
            }
        }
    }

    public class TransactionBuilder {

        private final ECKey senderKey;
        private final byte[] receiverAddress;
        // changeable during building transaction's data
        private byte[] value = ByteUtil.longToBytesNoLeadZeroes(0);
        private byte[] data = ByteUtil.EMPTY_BYTE_ARRAY;
        private byte[] gasPrice = ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice());
        private byte[] gasLimit = ByteUtil.longToBytesNoLeadZeroes(3_000_000);

        private byte[] txHash;
        private CompletableFuture<TransactionReceipt> futureReceipt;

        private long submitBlockNumber;
        private long waitBlocksCount;

        public TransactionBuilder(ECKey senderKey, byte[] receiverAddress) {
            this.senderKey = senderKey;
            this.receiverAddress = receiverAddress;
        }

        public TransactionBuilder data(byte[] data) {
            this.data = data;
            return this;
        }

        public TransactionBuilder gasPrice(byte[] gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public TransactionBuilder gasPrice(BigInteger gasPrice) {
            return gasPrice(gasPrice.toByteArray());
        }

        public TransactionBuilder gasLimit(byte[] gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public TransactionBuilder gasLimit(BigInteger gasLimit) {
            return gasLimit(gasLimit.toByteArray());
        }

        public TransactionBuilder value(byte[] value) {
            this.value = value;
            return this;
        }

        public TransactionBuilder value(BigInteger value, Denomination denomination) {
            return value(value.multiply(denomination.value()).toByteArray());
        }

        public TransactionBuilder value(BigInteger value) {
            return value(value, Denomination.WEI);
        }

        private void buildAndSubmit() {
            byte[] nonce = ByteUtil.bigIntegerToBytes(ethereum.getRepository().getNonce(senderKey.getAddress()));
            Integer chainId = ethereum.getChainIdForNextBlock();

            Transaction tx = new Transaction(nonce, gasPrice, gasLimit, receiverAddress, value, data, chainId);
            tx.sign(senderKey);

            ethereum.submitTransaction(tx);

            this.txHash = tx.getHash();
            this.submitBlockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
        }

        private boolean isSubmitted() {
            return submitBlockNumber > 0;
        }

        private boolean isApplicable(TransactionReceipt receipt) {
            return isSubmitted() && FastByteComparisons.equal(receipt.getTransaction().getHash(), txHash);
        }

        private void complete(TransactionReceipt receipt) {
            if (!isSubmitted()) {
                throw new IllegalStateException("Cannot complete non submitted transaction.");
            }
            futureReceipt.complete(receipt);
        }

        private void completeExceptionally(String error, Object... args) {
            if (!isSubmitted()) {
                throw new IllegalStateException("Cannot complete non submitted transaction.");
            }
            String message = format("Transaction %s execution error: %s", toHexString(txHash, 4), format(error, args));
            futureReceipt.completeExceptionally(new RuntimeException(message));
        }

        public boolean isExpired(Block block) {
            return isSubmitted() && (block.getNumber() - submitBlockNumber) > waitBlocksCount;
        }

        public CompletableFuture<TransactionReceipt> submit(int waitBlocksCount) {
            if (futureReceipt != null) {
                return futureReceipt;
            }

            this.futureReceipt = new CompletableFuture<>();
            this.waitBlocksCount = waitBlocksCount;

            addToSubmitQueue(this);

            return futureReceipt.whenComplete((receipt, err) -> removeFromSubmitQueue(this));
        }

        public CompletableFuture<TransactionReceipt> submit() {
            return submit(15);
        }
    }

    private static String toHexString(byte[] bytes, int count) {
        return ByteUtil.toHexString(Arrays.copyOf(bytes, count));
    }

    private static ByteArrayWrapper wrap(byte[] bytes) {
        return new ByteArrayWrapper(bytes);
    }
}

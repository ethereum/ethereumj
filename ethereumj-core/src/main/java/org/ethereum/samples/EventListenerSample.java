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
package org.ethereum.samples;

import org.ethereum.core.Block;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.BlockReplay;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.listener.EventListener;
import org.ethereum.listener.TxStatus;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Sample usage of events listener API.
 * {@link EventListener}        Contract events listener
 * {@link BlockReplay}          Listener wrapper for pushing old blocks to any listener in addition to live data
 *
 *  - getting free Ether assuming we are running in test network
 *  - deploying contract with event, which we are going to track
 *  - calling contract and catching corresponding events
 *  - alternatively you could provide address of already deployed contract and
 *      replay any number of blocks in the past to process old events
 */
public class EventListenerSample extends TestNetSample {

    @Autowired
    SolidityCompiler compiler;

    @Autowired
    BlockStore blockStore;

    @Autowired
    TransactionStore transactionStore;

    @Autowired
    PendingStateImpl pendingState;

    // Change seed phrases
    protected final byte[] senderPrivateKey = sha3("cat".getBytes());
    protected final byte[] sender2PrivateKey = sha3("goat".getBytes());

    // If no contractAddress provided, deploys new contract, otherwise
    // replays events from already deployed contract
    String contractAddress = null;
//    String contractAddress = "cedf27de170a05cf1d1736f21e1f5ffc1cf22eef";

    String contract =
            "contract Sample {\n" +
                    "  int i;\n" +
                    "  event Inc(\n" +
                    "      address _from,\n" +
                    "      int _inc,\n" +
                    "      int _total\n" +
                    "  );  \n" +
                    "  \n" +
                    "  function inc(int n) {\n" +
                    "    i = i + n;\n" +
                    "    Inc(msg.sender, n, i);  \n" +
                    "  }  \n" +
                    "  \n" +
                    "  function get() returns (int) {\n" +
                    "    return i;  \n" +
                    "  }\n" +
                    "}  ";

    private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<ByteArrayWrapper, TransactionReceipt>());

    class IncEvent {
        IncEvent(String address, Long inc, Long total) {
            this.address = address;
            this.inc = inc;
            this.total = total;
        }

        String address;
        Long inc;
        Long total;

        @Override
        public String toString() {
            return "IncEvent{" +
                    "address='" + address + '\'' +
                    ", inc=" + inc +
                    ", total=" + total +
                    '}';
        }
    }

    class IncEventListener extends EventListener<IncEvent> {
        /**
         * Minimum required Tx block confirmations for the events
         * from this Tx to be confirmed
         * After this number of confirmations, event will fire {@link #processConfirmed(PendingEvent, IncEvent)}
         * on each confirmation
         */
        protected int blocksToConfirm = 32;
        /**
         * Minimum required Tx block confirmations for this Tx to be purged
         * from the tracking list
         * After this number of confirmations, event will not fire {@link #processConfirmed(PendingEvent, IncEvent)}
         */
        protected int purgeFromPendingsConfirmations = 40;

        public IncEventListener(PendingStateImpl pendingState) {
            super(pendingState);
        }

        public IncEventListener(PendingStateImpl pendingState, String contractABI, byte[] contractAddress) {
            super(pendingState);
            initContractAddress(contractABI, contractAddress);
            // Instead you can init with topic search,
            // so you could get events from all contracts with the same code
            // You could init listener only once
//            initContractTopic(contractABI, sha3("Inc(address,int256,int256)".getBytes()));
        }

        @Override
        protected IncEvent onEvent(CallTransaction.Invocation event, Block block, TransactionReceipt receipt, int txCount, EthereumListener.PendingTransactionState state) {
            // Processing raw event data to fill our model IncEvent
            if ("Inc".equals(event.function.name)) {
                String address = Hex.toHexString((byte[]) event.args[0]);
                Long inc = ((BigInteger) event.args[1]).longValue();
                Long total = ((BigInteger) event.args[2]).longValue();

                IncEvent incEvent = new IncEvent(address, inc, total);
                logger.info("Pending event: {}", incEvent);
                return incEvent;
            } else {
                logger.error("Unknown event: " + event);
            }
            return null;
        }

        @Override
        protected void pendingTransactionsUpdated() {
        }

        /**
         * Events are fired here on every block since blocksToConfirm to purgeFromPendingsConfirmations
         */
        void processConfirmed(PendingEvent evt, IncEvent event) {
            // +1 because on included block we have 1 confirmation
            long numberOfConfirmations = evt.bestConfirmingBlock.getNumber() - evt.includedTo.getNumber() + 1;
            logger.info("Confirmed event: {}, confirmations: {}", event, numberOfConfirmations);
        }

        @Override
        protected boolean pendingTransactionUpdated(PendingEvent evt) {
            if (evt.txStatus == TxStatus.REJECTED || evt.txStatus.confirmed >= blocksToConfirm) {
                evt.eventData.forEach(d -> processConfirmed(evt, d));
            }
            return evt.txStatus == TxStatus.REJECTED || evt.txStatus.confirmed >= purgeFromPendingsConfirmations;
        }
    }

    /**
     * Sample logic starts here when sync is done
     */
    @Override
    public void onSyncDone() throws Exception {
        ethereum.addListener(new EthereumListenerAdapter() {
            @Override
            public void onPendingTransactionUpdate(TransactionReceipt txReceipt, PendingTransactionState state, Block block) {
                ByteArrayWrapper txHashW = new ByteArrayWrapper(txReceipt.getTransaction().getHash());
                // Catching transaction errors
                if (txWaiters.containsKey(txHashW) && !txReceipt.isSuccessful()) {
                    txWaiters.put(txHashW, txReceipt);
                }
            }
        });
        requestFreeEther(ECKey.fromPrivate(senderPrivateKey).getAddress());
        requestFreeEther(ECKey.fromPrivate(sender2PrivateKey).getAddress());
        if (contractAddress == null) {
            deployContractAndTest();
        } else {
            replayOnly();
        }
    }

    public void requestFreeEther(byte[] addressBytes) {
        String address = "0x" + Hex.toHexString(addressBytes);
        logger.info("Checking address {} for available ether.", address);
        BigInteger balance = ethereum.getRepository().getBalance(addressBytes);
        logger.info("Address {} balance: {} wei", address, balance);
        BigInteger requiredBalance = BigInteger.valueOf(3_000_000 * ethereum.getGasPrice());
        if (balance.compareTo(requiredBalance) < 0) {
            logger.info("Insufficient funds for address {}, requesting free ether", address);
            try {
                String result = postQuery("https://ropsten.faucet.b9lab.com/tap", "{\"toWhom\":\"" + address + "\"}");
                logger.info("Answer from free Ether API: {}", result);
                waitForEther(addressBytes, requiredBalance);
            } catch (Exception ex) {
                logger.error("Error during request of free Ether,", ex);
            }
        }
    }

    private String postQuery(String endPoint, String json) throws IOException {
        URL url = new URL(endPoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = null;
        try (Scanner scanner = new Scanner(in, "UTF-8")) {
            result =  scanner.useDelimiter("\\A").next();
        }

        in.close();
        conn.disconnect();

        return result;
    }

    private void waitForEther(byte[] address, BigInteger requiredBalance) throws InterruptedException {
        while(true) {
            BigInteger balance = ethereum.getRepository().getBalance(address);
            if (balance.compareTo(requiredBalance) > 0) {
                logger.info("Address {} successfully funded. Balance: {} wei", "0x" + Hex.toHexString(address), balance);
                break;
            }
            synchronized (this) {
                wait(20000);
            }
        }
    }

    /**
     *  - Deploys contract
     *  - Adds events listener
     *  - Calls contract from 2 different addresses
     */
    private void deployContractAndTest() throws Exception {
        ethereum.addListener(new EthereumListenerAdapter() {
            // when block arrives look for our included transactions
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                EventListenerSample.this.onBlock(block, receipts);
            }
        });

        CompilationResult.ContractMetadata metadata = compileContract();

        logger.info("Sending contract to net and waiting for inclusion");
        TransactionReceipt receipt = sendTxAndWait(new byte[0], Hex.decode(metadata.bin), senderPrivateKey);

        if (!receipt.isSuccessful()) {
            logger.error("Some troubles creating a contract: " + receipt.getError());
            return;
        }

        byte[] address = receipt.getTransaction().getContractAddress();
        logger.info("Contract created: " + Hex.toHexString(address));

        IncEventListener eventListener = new IncEventListener(pendingState, metadata.abi, address);
        ethereum.addListener(eventListener.listener);

        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        contractIncCall(senderPrivateKey, 777, metadata.abi, address);
        contractIncCall(sender2PrivateKey, 555, metadata.abi, address);

        ProgramResult r = ethereum.callConstantFunction(Hex.toHexString(address),
                contract.getByName("get"));
        Object[] ret = contract.getByName("get").decodeResult(r.getHReturn());
        logger.info("Current contract data member value: " + ret[0]);
    }

    /**
     * Replays contract events for old blocks
     * using {@link BlockReplay} with {@link EventListener}
     */
    private void replayOnly() throws Exception {
        logger.info("Contract already deployed to address 0x{}, using it", contractAddress);
        CompilationResult.ContractMetadata metadata = compileContract();
        byte[] address = Hex.decode(contractAddress);
        IncEventListener eventListener = new IncEventListener(pendingState, metadata.abi, address);
        BlockReplay blockReplay = new BlockReplay(blockStore, transactionStore, eventListener.listener,
                blockStore.getMaxNumber() - 5000);
        ethereum.addListener(blockReplay);
        blockReplay.replayAsync();
    }

    private CompilationResult.ContractMetadata compileContract() throws IOException {
        logger.info("Compiling contract...");
        SolidityCompiler.Result result = compiler.compileSrc(contract.getBytes(), true, true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (result.isFailed()) {
            throw new RuntimeException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.contracts.isEmpty()) {
            throw new RuntimeException("Compilation failed, no contracts returned:\n" + result.errors);
        }
        CompilationResult.ContractMetadata metadata = res.contracts.values().iterator().next();
        if (metadata.bin == null || metadata.bin.isEmpty()) {
            throw new RuntimeException("Compilation failed, no binary returned:\n" + result.errors);
        }

        return metadata;
    }

    private void contractIncCall(byte[] privateKey, int incAmount,
                                 String contractABI, byte[] contractAddress) throws InterruptedException {
        logger.info("Calling the contract function 'inc'");
        CallTransaction.Contract contract = new CallTransaction.Contract(contractABI);
        CallTransaction.Function inc = contract.getByName("inc");
        byte[] functionCallBytes = inc.encode(incAmount);
        TransactionReceipt receipt = sendTxAndWait(contractAddress, functionCallBytes, privateKey);
        if (!receipt.isSuccessful()) {
            logger.error("Some troubles invoking the contract: " + receipt.getError());
            return;
        }
        logger.info("Contract modified!");
    }

    protected TransactionReceipt sendTxAndWait(byte[] receiveAddress,
                                               byte[] data, byte[] privateKey) throws InterruptedException {
        BigInteger nonce = ethereum.getRepository().getNonce(ECKey.fromPrivate(privateKey).getAddress());
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                receiveAddress,
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                ethereum.getChainIdForNextBlock());
        tx.sign(ECKey.fromPrivate(privateKey));

        logger.info("<=== Sending transaction: " + tx);
        ByteArrayWrapper txHashW = new ByteArrayWrapper(tx.getHash());
        txWaiters.put(txHashW, null);
        ethereum.submitTransaction(tx);

        return waitForTx(txHashW);
    }

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

    protected TransactionReceipt waitForTx(ByteArrayWrapper txHashW) throws InterruptedException {
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

        class Config extends TestNetConfig{
            @Override
            @Bean
            public TestNetSample sampleBean() {
                return new EventListenerSample();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}

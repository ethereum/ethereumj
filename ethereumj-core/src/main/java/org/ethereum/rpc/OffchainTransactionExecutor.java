package org.ethereum.rpc;

import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * Created by Sergio on 03/11/2015.
 * Similar functionality you can find in
 * rootstockJ\ethereumj-core\src\main\java\org\ethereum\jsontestsuite\TestRunner.java :runTestCase
 * But I think this is much more clear.
 */
public class OffchainTransactionExecutor {

    public BlockHeader header;
    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final Logger logger = LoggerFactory.getLogger("offchain");

    @Autowired
    private Repository repository;
    private Repository track;


    @Autowired
    private BlockStore blockStore;
    // Must make sure that this blockStore is never modified, only read.
    // It seems that the ONLY use of blockStore is in Program.java:
    // .. getBlockHash(int index) {
    //.. this.invoke.getBlockStore().getBlockHashByNumber(index)
    // This is because the VM (VM.java) has the opcode BLOCKHASH

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    public BlockHeader getEmptyBlockHeader() {
        return new BlockHeader(
                EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                0, EMPTY_ARRAY, 0,
                0,
                EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY
        );
    }
    public List<TransactionReceipt> executeTransactions(List<Transaction> txList) {
        // There is a method called callConstantFunction in EthereumImpl.java that
        // executes transactions offline BUT does not store receipts nor allows to
        // set gastPrice. This method allows fine grained debugging.

        if (header==null)
            header = getEmptyBlockHeader();
        Block block = new Block(header,txList,null);
        logger.info("executeTransactions: block: [{}] tx.list: [{}]", block.getNumber(), block.getTransactionsList().size());
        long saveTime = System.nanoTime();
        int i = 1;
        long totalGasUsed = 0;
        List<TransactionReceipt> receipts = new ArrayList<>();

        for (Transaction tx : block.getTransactionsList()) {
            logger.info("executeTransactions: [{}] tx: [{}] ", block.getNumber(), i);

            TransactionExecutor executor = new TransactionExecutor(tx, block.getCoinbase(),
                    track, blockStore,
                    programInvokeFactory, block, null, totalGasUsed);

            executor.setLocalCall(true);
            executor.init();
            executor.execute();
            executor.go();
            executor.finalization();

            totalGasUsed += executor.getGasUsed();

            // Don't commit: changes must stay phantom track.commit();

            TransactionReceipt receipt = new TransactionReceipt();
            receipt.setCumulativeGas(totalGasUsed);
            receipt.setPostTxState(repository.getRoot());
            receipt.setTransaction(tx);
            receipt.setLogInfoList(executor.getVMLogs());
            // The result may also be needed executor.getResult();

            logger.info("executeTransactions: block: [{}] executed tx: [{}] \n  state: [{}]", block.getNumber(), i,
                    Hex.toHexString(repository.getRoot()));

            logger.info("[{}] ", receipt.toString());

            if (logger.isInfoEnabled())
                logger.info("tx[{}].receipt: [{}] ", i, Hex.toHexString(receipt.getEncoded()));

            receipts.add(receipt);
        }

        //track.commit();

        long totalTime = System.nanoTime() - saveTime;
        logger.info("block: num: [{}] hash: [{}], executed after: [{}]nano", block.getNumber(), block.getShortHash(), totalTime);

        return receipts;
    }

}

package org.ethereum.jsontestsuite.suite.runners;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.jsontestsuite.suite.TransactionTestCase;
import org.ethereum.jsontestsuite.suite.Utils;
import org.ethereum.jsontestsuite.suite.builder.TransactionBuilder;
import org.ethereum.jsontestsuite.suite.validators.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TransactionTestRunner {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    public static List<String> run(TransactionTestCase transactionTestCase2) {
        return new TransactionTestRunner(transactionTestCase2).runImpl();
    }

    protected TransactionTestCase transactionTestCase;
    protected Transaction transaction = null;
    protected Transaction expectedTransaction;
    protected long blockNumber;
    protected BlockchainConfig blockchainConfig;

    public TransactionTestRunner(TransactionTestCase transactionTestCase) {
        this.transactionTestCase = transactionTestCase;
    }

    public List<String> runImpl() {

        blockNumber = Utils.parseLong(transactionTestCase.getBlocknumber());
        logger.info("Block number: {}", blockNumber);
        this.blockchainConfig = SystemProperties.getDefault().getBlockchainConfig().getConfigForBlock(blockNumber);

        byte[] rlp = Utils.parseData(transactionTestCase.getRlp());
        try {
            transaction = new Transaction(rlp);
            transaction.rlpParse();
        } catch (Exception e) {
            transaction = null;
        }
        logger.info("Transaction: {}", transaction);

        expectedTransaction = transactionTestCase.getTransaction() == null ? null : TransactionBuilder.build(transactionTestCase.getTransaction());
        logger.info("Expected transaction: {}", expectedTransaction);

        // Not enough GAS
        if (transaction != null) {
            long basicTxCost = blockchainConfig.getTransactionCost(transaction);
            if (new BigInteger(1, transaction.getGasLimit()).compareTo(BigInteger.valueOf(basicTxCost)) < 0) {
                transaction = null;
            }
        }

        // Transaction signature verification
        String acceptFail = null;
        boolean shouldAccept = transaction != null && blockchainConfig.acceptTransactionSignature(transaction);
        if (!shouldAccept) transaction = null;
        if (shouldAccept != (expectedTransaction != null)) {
            acceptFail = "Transaction shouldn't be accepted";
        }

        String wrongSender = null;
        String wrongHash = null;
        if (transaction != null && expectedTransaction != null) {
            // Verifying sender
            if (!Hex.toHexString(transaction.getSender()).equals(transactionTestCase.getSender()))
                wrongSender = "Sender is incorrect in parsed transaction";
            // Verifying hash
            if (!Hex.toHexString(transaction.getHash()).equals(transactionTestCase.getHash()))
                wrongHash = "Hash is incorrect in parsed transaction";
        }

        logger.info("--------- POST Validation---------");
        List<String> results = new ArrayList<>();

        ArrayList<String> outputSummary =
                TransactionValidator.valid(transaction, expectedTransaction);

        results.addAll(outputSummary);
        if (acceptFail != null) results.add(acceptFail);
        if (wrongSender != null) results.add(wrongSender);
        if (wrongHash != null) results.add(wrongHash);

        for (String result : results) {
            logger.error(result);
        }

        logger.info("\n\n");
        return results;
    }
}

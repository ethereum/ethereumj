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

        blockNumber = transactionTestCase.getBlocknumber() == null ? 0 : Utils.parseLong(transactionTestCase.getBlocknumber());
        logger.info("Block number: {}", blockNumber);
        this.blockchainConfig = SystemProperties.getDefault().getBlockchainConfig().getConfigForBlock(blockNumber);

        try {
            byte[] rlp = Utils.parseData(transactionTestCase.getRlp());
            transaction = new Transaction(rlp);
            transaction.verify();
        } catch (Exception e) {
            transaction = null;
        }
        if (transaction == null || transaction.getEncoded().length < 10000) {
            logger.info("Transaction: {}", transaction);
        } else {
            logger.info("Transaction data skipped because it's too big", transaction);
        }

        expectedTransaction = transactionTestCase.getTransaction() == null ? null : TransactionBuilder.build(transactionTestCase.getTransaction());
        if (expectedTransaction == null || expectedTransaction.getEncoded().length < 10000) {
            logger.info("Expected transaction: {}", expectedTransaction);
        } else {
            logger.info("Expected transaction data skipped because it's too big", transaction);
        }

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
            // NOTE: "hash" is not required field in test case
            if (transactionTestCase.getHash() != null &&
                    !Hex.toHexString(transaction.getHash()).equals(transactionTestCase.getHash()))
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

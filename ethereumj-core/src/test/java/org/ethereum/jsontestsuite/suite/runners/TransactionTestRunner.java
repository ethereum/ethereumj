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
import org.ethereum.core.Transaction;
import org.ethereum.jsontestsuite.suite.TransactionTestCase;
import org.ethereum.jsontestsuite.suite.Utils;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected byte[] expectedHash;
    protected byte[] expectedSender;
    protected long blockNumber;
    protected BlockchainConfig blockchainConfig;

    public TransactionTestRunner(TransactionTestCase transactionTestCase) {
        this.transactionTestCase = transactionTestCase;
    }

    public List<String> runImpl() {
        this.blockNumber = 0;
        this.blockchainConfig = transactionTestCase.getNetwork().getConfig().getConfigForBlock(blockNumber);

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

        this.expectedHash = (transactionTestCase.getExpectedHash() != null && !transactionTestCase.getExpectedHash().isEmpty())
                ? ByteUtil.hexStringToBytes(transactionTestCase.getExpectedHash())
                : null;
        this.expectedSender = (transactionTestCase.getExpectedRlp() != null && !transactionTestCase.getExpectedRlp().isEmpty())
                ? ByteUtil.hexStringToBytes(transactionTestCase.getExpectedRlp())
                : null;
        logger.info("Expected transaction: [hash: {}, sender: {}]",
                ByteUtil.toHexString(expectedHash), ByteUtil.toHexString(expectedSender));

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
        if (shouldAccept == (expectedSender == null)) {
            acceptFail = "Transaction shouldn't be accepted";
        }

        String wrongSender = null;
        String wrongHash = null;
        if (transaction != null && expectedHash != null) {
            // Verifying sender
            if (!FastByteComparisons.equal(transaction.getSender(), expectedSender))
                wrongSender = "Sender is incorrect in parsed transaction";
            // Verifying hash
            // NOTE: "hash" is not required field in test case
            if (expectedHash != null &&
                    !FastByteComparisons.equal(transaction.getHash(), expectedHash))
                wrongHash = "Hash is incorrect in parsed transaction";
        }

        List<String> results = new ArrayList<>();
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

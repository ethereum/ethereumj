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
package org.ethereum.core;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.program.InternalTransaction;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.size;
import static org.ethereum.util.ByteUtil.toHexString;
import static org.junit.Assert.*;

public class TransactionExecutionSummaryTest {


    @Test
    public void testRlpEncoding() {
        Transaction tx = randomTransaction();
        Set<DataWord> deleteAccounts = new HashSet<>(randomDataWords(10));
        List<LogInfo> logs = randomLogsInfo(5);

        final Map<DataWord, DataWord> readOnly = randomStorageEntries(20);
        final Map<DataWord, DataWord> changed = randomStorageEntries(5);
        Map<DataWord, DataWord> all = new HashMap<DataWord, DataWord>() {{
            putAll(readOnly);
            putAll(changed);
        }};

        BigInteger gasLeftover = new BigInteger("123");
        BigInteger gasRefund = new BigInteger("125");
        BigInteger gasUsed = new BigInteger("556");

        final int nestedLevelCount = 5000;
        final int countByLevel = 1;
        List<InternalTransaction> internalTransactions = randomInternalTransactions(tx, nestedLevelCount, countByLevel);

        byte[] result = randomBytes(32);


        byte[] encoded = new TransactionExecutionSummary.Builder(tx)
                .deletedAccounts(deleteAccounts)
                .logs(logs)
                .touchedStorage(all, changed)
                .gasLeftover(gasLeftover)
                .gasRefund(gasRefund)
                .gasUsed(gasUsed)
                .internalTransactions(internalTransactions)
                .result(result)
                .build()
                .getEncoded();


        TransactionExecutionSummary summary = new TransactionExecutionSummary(encoded);
        assertArrayEquals(tx.getHash(), summary.getTransactionHash());

        assertEquals(size(deleteAccounts), size(summary.getDeletedAccounts()));
        for (DataWord account : summary.getDeletedAccounts()) {
            assertTrue(deleteAccounts.contains(account));
        }

        assertEquals(size(logs), size(summary.getLogs()));
        for (int i = 0; i < logs.size(); i++) {
            assertLogInfoEquals(logs.get(i), summary.getLogs().get(i));
        }

        assertStorageEquals(all, summary.getTouchedStorage().getAll());
        assertStorageEquals(changed, summary.getTouchedStorage().getChanged());
        assertStorageEquals(readOnly, summary.getTouchedStorage().getReadOnly());

        assertEquals(gasRefund, summary.getGasRefund());
        assertEquals(gasLeftover, summary.getGasLeftover());
        assertEquals(gasUsed, summary.getGasUsed());

        assertEquals(nestedLevelCount * countByLevel, size(internalTransactions));

        assertArrayEquals(result, summary.getResult());
    }

    private static void assertStorageEquals(Map<DataWord, DataWord> expected, Map<DataWord, DataWord> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (DataWord key : expected.keySet()) {
            DataWord actualValue = actual.get(key);
            assertNotNull(actualValue);
            assertArrayEquals(expected.get(key).getData(), actualValue.getData());
        }
    }

    private static void assertLogInfoEquals(LogInfo expected, LogInfo actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertArrayEquals(expected.getAddress(), actual.getAddress());
        assertEquals(size(expected.getTopics()), size(actual.getTopics()));
        for (int i = 0; i < size(expected.getTopics()); i++) {
            assertArrayEquals(expected.getTopics().get(i).getData(), actual.getTopics().get(i).getData());
        }
        assertArrayEquals(expected.getData(), actual.getData());
    }

    private static Map<DataWord, DataWord> randomStorageEntries(int count) {
        Map<DataWord, DataWord> result = new HashMap<>();
        for (int i = 0; i < count; i++) {
            result.put(randomDataWord(), randomDataWord());
        }
        return result;
    }

    private static LogInfo randomLogInfo() {
        return new LogInfo(randomBytes(20), randomDataWords(5), randomBytes(8));
    }

    private static List<LogInfo> randomLogsInfo(int count) {
        List<LogInfo> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(randomLogInfo());
        }
        return result;
    }

    private static DataWord randomDataWord() {
        return new DataWord(randomBytes(32));
    }

    private static DataWord randomAddress() {
        return new DataWord(randomBytes(20));
    }

    private static List<DataWord> randomDataWords(int count) {
        List<DataWord> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(randomDataWord());
        }
        return result;
    }

    private static InternalTransaction randomInternalTransaction(Transaction parent, int deep, int index) {
        return new InternalTransaction(parent.getHash(), deep, index, randomBytes(1), DataWord.ZERO, DataWord.ZERO,
                parent.getReceiveAddress(), randomBytes(20), randomBytes(2), randomBytes(64), "test note");
    }

    private static List<InternalTransaction> randomInternalTransactions(Transaction parent, int nestedLevelCount, int countByLevel) {
        List<InternalTransaction> result = new ArrayList<>();
        if (nestedLevelCount > 0) {
            for (int index = 0; index < countByLevel; index++) {
                result.add(randomInternalTransaction(parent, nestedLevelCount, index));
            }
            result.addAll(0, randomInternalTransactions(result.get(result.size() - 1), nestedLevelCount - 1, countByLevel));
        }

        return result;
    }

    private static Transaction randomTransaction() {
        Transaction transaction = Transaction.createDefault(toHexString(randomBytes(20)), new BigInteger(randomBytes(2)), new BigInteger(randomBytes(1)), null);
        transaction.sign(randomBytes(32));
        return transaction;
    }

    private static byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        new Random().nextBytes(bytes);
        return bytes;
    }
}
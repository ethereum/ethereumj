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

import org.ethereum.util.Functional;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.ethereum.util.ByteUtil.toHexString;

public class BlockSummary {

    private final Block block;
    private final Map<byte[], BigInteger> rewards;
    private final List<TransactionReceipt> receipts;
    private final List<TransactionExecutionSummary> summaries;
    private BigInteger totalDifficulty = BigInteger.ZERO;

    public BlockSummary(byte[] rlp) {
        RLPList summary = (RLPList) RLP.decode2(rlp).get(0);

        this.block = new Block(summary.get(0).getRLPData());
        this.rewards = decodeRewards((RLPList) summary.get(1));
        this.summaries = decodeSummaries((RLPList) summary.get(2));
        this.receipts = new ArrayList<>();

        Map<String, TransactionReceipt> receiptByTxHash = decodeReceipts((RLPList) summary.get(3));
        for (Transaction tx : this.block.getTransactionsList()) {
            TransactionReceipt receipt = receiptByTxHash.get(toHexString(tx.getHash()));
            receipt.setTransaction(tx);

            this.receipts.add(receipt);
        }
    }

    public BlockSummary(Block block, Map<byte[], BigInteger> rewards, List<TransactionReceipt> receipts, List<TransactionExecutionSummary> summaries) {
        this.block = block;
        this.rewards = rewards;
        this.receipts = receipts;
        this.summaries = summaries;
    }

    public Block getBlock() {
        return block;
    }

    public List<TransactionReceipt> getReceipts() {
        return receipts;
    }

    public List<TransactionExecutionSummary> getSummaries() {
        return summaries;
    }

    /**
     * All the mining rewards paid out for this block, including the main block rewards, uncle rewards, and transaction fees.
     */
    public Map<byte[], BigInteger> getRewards() {
        return rewards;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public byte[] getEncoded() {
        return RLP.encodeList(
                block.getEncoded(),
                encodeRewards(rewards),
                encodeSummaries(summaries),
                encodeReceipts(receipts)
        );
    }

    private static <T> byte[] encodeList(List<T> entries, Functional.Function<T, byte[]> encoder) {
        byte[][] result = new byte[entries.size()][];
        for (int i = 0; i < entries.size(); i++) {
            result[i] = encoder.apply(entries.get(i));
        }

        return RLP.encodeList(result);
    }

    private static <T> List<T> decodeList(RLPList list, Functional.Function<byte[], T> decoder) {
        List<T> result = new ArrayList<>();
        for (RLPElement item : list) {
            result.add(decoder.apply(item.getRLPData()));
        }
        return result;
    }

    private static <K, V> byte[] encodeMap(Map<K, V> map, Functional.Function<K, byte[]> keyEncoder, Functional.Function<V, byte[]> valueEncoder) {
        byte[][] result = new byte[map.size()][];
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            byte[] key = keyEncoder.apply(entry.getKey());
            byte[] value = valueEncoder.apply(entry.getValue());
            result[i++] = RLP.encodeList(key, value);
        }
        return RLP.encodeList(result);
    }

    private static <K, V> Map<K, V> decodeMap(RLPList list, Functional.Function<byte[], K> keyDecoder, Functional.Function<byte[], V> valueDecoder) {
        Map<K, V> result = new HashMap<>();
        for (RLPElement entry : list) {
            K key = keyDecoder.apply(((RLPList) entry).get(0).getRLPData());
            V value = valueDecoder.apply(((RLPList) entry).get(1).getRLPData());
            result.put(key, value);
        }
        return result;
    }

    private static byte[] encodeSummaries(final List<TransactionExecutionSummary> summaries) {
        return encodeList(summaries, new Functional.Function<TransactionExecutionSummary, byte[]>() {
            @Override
            public byte[] apply(TransactionExecutionSummary summary) {
                return summary.getEncoded();
            }
        });
    }

    private static List<TransactionExecutionSummary> decodeSummaries(RLPList summaries) {
        return decodeList(summaries, new Functional.Function<byte[], TransactionExecutionSummary>() {
            @Override
            public TransactionExecutionSummary apply(byte[] encoded) {
                return new TransactionExecutionSummary(encoded);
            }
        });
    }

    private static byte[] encodeReceipts(List<TransactionReceipt> receipts) {
        Map<String, TransactionReceipt> receiptByTxHash = new HashMap<>();
        for (TransactionReceipt receipt : receipts) {
            receiptByTxHash.put(toHexString(receipt.getTransaction().getHash()), receipt);
        }

        return encodeMap(receiptByTxHash, new Functional.Function<String, byte[]>() {
            @Override
            public byte[] apply(String txHash) {
                return RLP.encodeString(txHash);
            }
        }, new Functional.Function<TransactionReceipt, byte[]>() {
            @Override
            public byte[] apply(TransactionReceipt receipt) {
                return receipt.getEncoded();
            }
        });
    }

    private static Map<String, TransactionReceipt> decodeReceipts(RLPList receipts) {
        return decodeMap(receipts, new Functional.Function<byte[], String>() {
            @Override
            public String apply(byte[] bytes) {
                return new String(bytes);
            }
        }, new Functional.Function<byte[], TransactionReceipt>() {
            @Override
            public TransactionReceipt apply(byte[] encoded) {
                return new TransactionReceipt(encoded);
            }
        });
    }

    private static byte[] encodeRewards(Map<byte[], BigInteger> rewards) {
        return encodeMap(rewards, new Functional.Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] bytes) {
                return RLP.encodeElement(bytes);
            }
        }, new Functional.Function<BigInteger, byte[]>() {
            @Override
            public byte[] apply(BigInteger reward) {
                return RLP.encodeBigInteger(reward);
            }
        });
    }

    private static Map<byte[], BigInteger> decodeRewards(RLPList rewards) {
        return decodeMap(rewards, new Functional.Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] bytes) {
                return bytes;
            }
        }, new Functional.Function<byte[], BigInteger>() {
            @Override
            public BigInteger apply(byte[] bytes) {
                return isEmpty(bytes) ? BigInteger.ZERO : new BigInteger(1, bytes);
            }
        });
    }
}

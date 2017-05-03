package org.ethereum.core;

import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLPList;
import org.ethereum.util.Utils;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Created by jim on 03/05/17.
 */
public interface IBlockHeader {
    int NONCE_LENGTH = 8;
    int MAX_HEADER_SIZE = 592;

    boolean isGenesis();

    byte[] getParentHash();

    byte[] getUnclesHash();

    void setUnclesHash(byte[] unclesHash);

    byte[] getCoinbase();

    void setCoinbase(byte[] coinbase);

    byte[] getStateRoot();

    void setStateRoot(byte[] stateRoot);

    byte[] getTxTrieRoot();

    void setReceiptsRoot(byte[] receiptTrieRoot);

    byte[] getReceiptsRoot();

    void setTransactionsRoot(byte[] stateRoot);

    byte[] getLogsBloom();

    byte[] getDifficulty();

    BigInteger getDifficultyBI();

    void setDifficulty(byte[] difficulty);

    long getTimestamp();

    void setTimestamp(long timestamp);

    long getNumber();

    void setNumber(long number);

    byte[] getGasLimit();

    void setGasLimit(byte[] gasLimit);

    long getGasUsed();

    void setGasUsed(long gasUsed);

    byte[] getMixHash();

    void setMixHash(byte[] mixHash);

    byte[] getExtraData();

    byte[] getNonce();

    void setNonce(byte[] nonce);

    void setLogsBloom(byte[] logsBloom);

    void setExtraData(byte[] extraData);

    byte[] getHash();

    byte[] getEncoded();

    byte[] getEncodedWithoutNonce();

    byte[] getEncoded(boolean withNonce);

    byte[] getUnclesEncoded(List<IBlockHeader> uncleList);

    byte[] getPowBoundary();

    void setParentHash(byte[] parentHash);

    void setTxTrieRoot(byte[] txTrieRoot);

    byte[] getReceiptTrieRoot();

    void setReceiptTrieRoot(byte[] receiptTrieRoot);


    interface View {
        static String getShortDescr(IBlockHeader blockHeader) {
            return "#" + blockHeader.getNumber() + " (" + Hex.toHexString(blockHeader.getHash()).substring(0,6) + " <~ "
                    + Hex.toHexString(blockHeader.getParentHash()).substring(0,6) + ")";
        }

        static String toFlatString(IBlockHeader blockHeader) {
            return toStringWithSuffix(blockHeader, "");
        }

        static String toStringWithSuffix(IBlockHeader blockHeader, final String suffix) {
            return "  hash=" + toHexString(blockHeader.getHash()) + suffix +
                    "  parentHash=" + toHexString(blockHeader.getParentHash()) + suffix +
                    "  unclesHash=" + toHexString(blockHeader.getUnclesHash()) + suffix +
                    "  coinbase=" + toHexString(blockHeader.getCoinbase()) + suffix +
                    "  stateRoot=" + toHexString(blockHeader.getStateRoot()) + suffix +
                    "  txTrieHash=" + toHexString(blockHeader.getTxTrieRoot()) + suffix +
                    "  receiptsTrieHash=" + toHexString(blockHeader.getReceiptTrieRoot()) + suffix +
                    "  difficulty=" + toHexString(blockHeader.getDifficulty()) + suffix +
                    "  number=" + blockHeader.getNumber() + suffix +
                    "  gasLimit=" + toHexString(blockHeader.getGasLimit()) + suffix +
                    "  gasUsed=" + blockHeader.getGasUsed() + suffix +
                    "  timestamp=" + blockHeader.getTimestamp() + " (" + Utils.longToDateTime(blockHeader.getTimestamp()) + ")" + suffix +
                    "  extraData=" + toHexString(blockHeader.getExtraData()) + suffix +
                    "  mixHash=" + toHexString(blockHeader.getMixHash()) + suffix +
                    "  nonce=" + toHexString(blockHeader.getNonce()) + suffix;
        }

        static BigInteger calcDifficulty(BlockchainNetConfig config, IBlockHeader parent, IBlockHeader blockHeader) {
              return  config.getConfigForBlock(blockHeader.getNumber()).
                      calcDifficulty(blockHeader, parent);
          }

        static byte[] calcPowValue(IBlockHeader blockHeader) {

            // nonce bytes are expected in Little Endian order, reverting
            byte[] nonceReverted = Arrays.reverse(blockHeader.getNonce());
            byte[] hashWithoutNonce = HashUtil.sha3(blockHeader.getEncodedWithoutNonce());

            byte[] seed = Arrays.concatenate(hashWithoutNonce, nonceReverted);
            byte[] seedHash = HashUtil.sha512(seed);

            byte[] concat = Arrays.concatenate(seedHash, blockHeader.getMixHash());
            return HashUtil.sha3(concat);
        }
    }


    interface Factory {
        static BlockHeader createBlockHeader(byte... encoded) {
            return new BlockHeader(encoded);
        }

        static BlockHeader decodeBlockHeader(RLPList rlpHeader) {
            return new BlockHeader(rlpHeader);
        }

        static BlockHeader assembleBlockHeader(byte[] parentHash, byte[] unclesHash, byte[] coinbase,
                                               byte[] logsBloom, byte[] difficulty, long number,
                                               byte[] gasLimit, long gasUsed, long timestamp,
                                               byte[] mixHash, byte[] nonce, byte... extraData) {
            return new BlockHeader(parentHash, unclesHash, coinbase, logsBloom, difficulty, number, gasLimit, gasUsed, timestamp, mixHash, nonce, extraData);
        }
    }
}

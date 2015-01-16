package org.ethereum.crypto;

import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.util.LRUMap;
import org.ethereum.util.RLP;
import org.ethereum.util.Utils;

import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.Arrays.copyOfRange;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

public class HashUtil {

    private static final int MAX_ENTRIES = 100; // Should contain most commonly hashed values
    private static LRUMap<ByteArrayWrapper, byte[]> sha3Cache = new LRUMap<>(0, MAX_ENTRIES);
    public static final byte[] EMPTY_DATA_HASH = sha3(EMPTY_BYTE_ARRAY);
    public static final byte[] EMPTY_LIST_HASH = sha3(RLP.encodeList());
    public static final byte[] EMPTY_TRIE_HASH = sha3(RLP.encodeElement(EMPTY_BYTE_ARRAY));

    private static final MessageDigest sha256digest;

    static {
        try {
            sha256digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }

    public static byte[] sha256(byte[] input) {
        return sha256digest.digest(input);
    }

    public static byte[] sha3(byte[] input) {
        ByteArrayWrapper inputByteArray = new ByteArrayWrapper(input);
        byte[] result = sha3Cache.get(inputByteArray);
        if (result != null)
            return result;
        result = SHA3Helper.sha3(input);
        sha3Cache.put(inputByteArray, result);
        return result;
    }

    public static byte[] ripemd160(byte[] message) {
        Digest digest = new RIPEMD160Digest();
        if (message != null) {
            byte[] resBuf = new byte[digest.getDigestSize()];
            digest.update(message, 0, message.length);
            digest.doFinal(resBuf, 0);
            return resBuf;
        }
        throw new NullPointerException("Can't hash a NULL value");
    }


    /**
     * Calculates RIGTMOST160(SHA3(input)). This is used in address calculations.
     */
    public static byte[] sha3omit12(byte[] input) {
        byte[] hash = sha3(input);
        return copyOfRange(hash, 12, hash.length);
    }

    /**
     * The way to calculate new address inside ethereum
     *
     * @param addr - creating addres
     * @param nonce - nonce of creating address
     * @return new address
     */
    public static byte[] calcNewAddr(byte[] addr, byte[] nonce) {

        byte[] encSender = RLP.encodeElement(addr);
        byte[] encNonce = RLP.encodeBigInteger(new BigInteger(1, nonce));

        return sha3omit12(RLP.encodeList(encSender, encNonce));
    }

    /**
     * @see #doubleDigest(byte[], int, int)
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in Bitcoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (sha256digest) {
            sha256digest.reset();
            sha256digest.update(input, offset, length);
            byte[] first = sha256digest.digest();
            return sha256digest.digest(first);
        }
    }

    /**
     * @return generates random peer id for the HelloMessage
     */
    public static byte[] randomPeerId() {

        byte[] peerIdBytes = new BigInteger(512, Utils.getRandom()).toByteArray();

        final String peerId;
        if (peerIdBytes.length > 64)
            peerId = Hex.toHexString(peerIdBytes, 1, 64);
        else
            peerId = Hex.toHexString(peerIdBytes);

        return Hex.decode(peerId);
    }
}

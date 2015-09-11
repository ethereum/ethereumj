package org.ethereum.core;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.Arrays;

/**
 * Keeps Proof-of-Work logic
 *
 * @author Mikhail Kalinin
 * @since 31.08.2015
 */
public class PowHelper {

    /**
     * Compares proof value against its boundary for the block header
     *
     * @param header block header
     * @return true if proof value is less than or equal to the boundary, false otherwise
     */
    public static boolean isValid(BlockHeader header) {
        byte[] proof = calculateProof(header.getEncodedWithoutNonce(), header.getNonce(), header.getMixHash());
        byte[] boundary = header.getPowBoundary();
        return FastByteComparisons.compareTo(proof, 0, 32, boundary, 0, 32) <= 0;
    }

    private static byte[] calculateProof(byte[] encodedWithoutNonce, byte[] nonce, byte[] mixHash) {
        // nonce bytes are expected in Little Endian order, reverting
        byte[] nonceReverted = Arrays.reverse(nonce);
        byte[] hashWithoutNonce = HashUtil.sha3(encodedWithoutNonce);

        byte[] seed = Arrays.concatenate(hashWithoutNonce, nonceReverted);
        byte[] seedHash = HashUtil.sha512(seed);

        byte[] concat = Arrays.concatenate(seedHash, mixHash);
        return HashUtil.sha3(concat);
    }
}

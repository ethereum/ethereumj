package org.ethereum.validator;

import org.ethereum.core.BlockHeader;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.Arrays;

/**
 * Checks proof value against its boundary for the block header
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class ProofOfWorkRule extends BlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header) {

        errors.clear();

        byte[] proof = calculateProof(header.getEncodedWithoutNonce(), header.getNonce(), header.getMixHash());
        byte[] boundary = header.getPowBoundary();

        if (FastByteComparisons.compareTo(proof, 0, 32, boundary, 0, 32) > 0) {
            errors.add("proofValue > header.getPowBoundary()");
            return false;
        }

        return true;
    }

    private byte[] calculateProof(byte[] encodedWithoutNonce, byte[] nonce, byte[] mixHash) {

        // nonce bytes are expected in Little Endian order, reverting
        byte[] nonceReverted = Arrays.reverse(nonce);
        byte[] hashWithoutNonce = HashUtil.sha3(encodedWithoutNonce);

        byte[] seed = Arrays.concatenate(hashWithoutNonce, nonceReverted);
        byte[] seedHash = HashUtil.sha512(seed);

        byte[] concat = Arrays.concatenate(seedHash, mixHash);
        return HashUtil.sha3(concat);
    }
}

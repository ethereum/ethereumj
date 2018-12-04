package org.ethereum.sharding.crypto;

import java.math.BigInteger;
import java.util.List;

/**
 * Signature point of entry
 *
 * Signs and verifies message
 * Implements signature and public keys aggregation
 */
public interface Sign {

    /**
     * Generates private and public keys together
     * @return New {@link KeyPair} instance
     */
    KeyPair newKeyPair();

    /**
     * Derives public key from private
     */
    BigInteger privToPub(BigInteger privKey);

    /**
     * Sign the message
     */
    byte[] sign(byte[] msgHash, byte[] domain, BigInteger privateKey);

    /**
     * Verifies whether signature is made by signer with publicKey
     */
    boolean verify(byte[] signature, byte[] msgHash, BigInteger publicKey, byte[] domain);

    /**
     * Aggregates several signatures in one
     */
    byte[] aggSigns(List<byte[]> signatures);

    /**
     * Aggregates public keys
     */
    BigInteger aggPubs(List<BigInteger> pubKeys);

    class KeyPair {
        BigInteger sigKey;  // Signature (private key)
        BigInteger verKey;  // Verification (public key)
    }
}

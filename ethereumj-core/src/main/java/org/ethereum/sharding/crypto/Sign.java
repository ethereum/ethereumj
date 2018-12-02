package org.ethereum.sharding.crypto;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

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
    Signature sign(byte[] msgHash, BigInteger privateKey);

    /**
     * Verifies whether signature is made by signer with publicKey
     */
    boolean verify(Signature signature, byte[] msgHash, BigInteger publicKey);

    /**
     * Aggregates several signatures in one
     */
    Signature aggSigns(List<Signature> signatures);

    /**
     * Aggregates public keys
     */
    BigInteger aggPubs(List<BigInteger> pubKeys);

    class Signature {
        public BigInteger value;

        public Signature(BigInteger value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Signature signature = (Signature) o;
            return Objects.equals(value, signature.value);
        }

        @Override
        public String toString() {
            return "Signature{" +
                    "value=" + value +
                    '}';
        }
    }

    class KeyPair {
        BigInteger sigKey;  // Signature (private key)
        BigInteger verKey;  // Verification (public key)
    }
}

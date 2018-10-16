package org.ethereum.sharding.crypto;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Signature utilities
 * Signature should be implemented using BLS
 */
public interface Sign {

    /**
     * Sign the message
     */
    Signature sign(byte[] msg, BigInteger privateKey);

    /**
     * Verifies whether signature is made by signer with publicKey
     */
    boolean verify(Signature signature, byte[] msg, BigInteger publicKey);

    /**
     * Calculates public key from private
     */
    BigInteger privToPub(BigInteger privKey);

    /**
     * Aggregates several signatures in one
     */
    Signature aggSigns(List<Signature> signatures);

    /**
     * Aggregates public keys
     */
    BigInteger aggPubs(List<BigInteger> pubKeys);

    class Signature {
        public BigInteger r;
        public BigInteger s;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Signature signature = (Signature) o;
            return Objects.equals(r, signature.r) &&
                    Objects.equals(s, signature.s);
        }

        @Override
        public String toString() {
            return "Signature{" +
                    "r=" + r +
                    ", s=" + s +
                    '}';
        }
    }
}

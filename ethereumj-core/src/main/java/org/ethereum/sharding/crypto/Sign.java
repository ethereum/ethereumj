package org.ethereum.sharding.crypto;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Signature utilities
 * Signature should be implemented using BLS
 */
public interface Sign {

    /**
     * Sign the message
     */
    Signature sign(byte[] msg, byte[] privateKey);

    /**
     * Verifies whether signature is made by signer with publicKey
     */
    boolean verify(Signature signature, byte[] publicKey);

    /**
     * Recovers public key using signature and hash of the message that was signed
     */
    byte[] recover(Signature signature, byte[] msgHash);

    /**
     * Aggregates several signatures in one
     */
    Signature aggSigns(Signature[] signatures);

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

package org.ethereum.sharding.crypto;

/**
 * Common interface for BLS381-12 implementation
 */
public interface BLS381 {

    Scalar generateRandomPrivate();

    Scalar restoreScalar(byte[] value);

    P1 restoreECP1(byte[] value);

    P2 restoreECP2(byte[] value);

    P2 generator2();

    P1 mapToECP1(byte[] value);

    FP12 pair(P2 pointECP2, P1 pointECP1);

    interface Scalar {
        byte[] asByteArray();
    }

    interface P1 {
        P1 mul(Scalar value);

        void add(P1 value);

        byte[] asByteArray();
    }

    interface P2 {
        P2 mul(Scalar value);

        void add(P2 value);

        byte[] asByteArray();
    }

    interface FP12 {
        boolean equals(FP12 other);
    }
}

package org.ethereum.sharding.crypto;

/**
 * Common interface for BLS381-12 implementation
 */
public interface BLS381 {

    /**
     * Generates random private key
     * Private key is correct scalar
     */
    Scalar generateRandomPrivate();

    /**
     * Restores scalar from byte[]
     */
    Scalar restoreScalar(byte[] value);

    /**
     * Restores point on elliptic curve #1
     * @return ECP1
     */
    P1 restoreECP1(byte[] value);

    /**
     * Restores point on elliptic curve #2
     * @return ECP2
     */
    P2 restoreECP2(byte[] value);

    /**
     * @return Generator of ECP1
     */
    P1 generator();

    // FIXME: REMOVE ME
    /**
     * Maps byte[] value to ECP2
     * @return  eligible mapping
     */
    @Deprecated
    P2 mapToECP2(byte[] value);

    /**
     * Pairing function
     * @param point2    Point on ECP2
     * @param point1    Point on ECP1
     * @return element of FP12
     */
    FP12 pair(P2 point2, P1 point1);

    /**
     * Represents scalar compliant with curve order
     */
    interface Scalar {
        byte[] asByteArray();
    }

    /**
     * Represents point on ECP1 (elliptic curve #1)
     */
    interface P1 {
        P1 mul(Scalar value);

        void add(P1 value);

        byte[] asByteArray();
    }

    /**
     * Represents point on ECP2 (elliptic curve #2)
     */
    interface P2 {
        P2 mul(Scalar value);

        void add(P2 value);

        byte[] asByteArray();
    }

    /**
     * Represents element of FP12 extension field
     */
    interface FP12 {
        boolean equals(FP12 other);
    }
}

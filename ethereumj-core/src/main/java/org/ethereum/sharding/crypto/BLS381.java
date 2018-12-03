package org.ethereum.sharding.crypto;

import java.math.BigInteger;

/**
 * Common interface for BLS381-12 implementation
 */
public interface BLS381 {

    BI generatePrivate();

    BI restorePrivate(BigInteger value);

    BI restorePrivate(byte[] value);

    ECP1Point restoreECP1(byte[] value);

    ECP2Point restoreECP2(byte[] value);

    ECP2Point generator2();

    ECP1Point mapToECP1(byte[] value);

    FP12Point pair(ECP2Point pointECP2, ECP1Point pointECP1);

    interface BI {
        BigInteger asBigInteger();

        byte[] asByteArray();
    }

    interface ECP1Point {
        ECP1Point mul(BI value);

        void add(ECP1Point value);

        BigInteger asBigInteger();

        byte[] asByteArray();
    }

    interface ECP2Point {
        ECP2Point mul(BI value);

        void add(ECP2Point value);

        BigInteger asBigInteger();

        byte[] asByteArray();
    }

    interface FP12Point {}
}

package org.ethereum.sharding.crypto;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.milagro.amcl.BLS381.FP12;
import org.apache.milagro.amcl.BLS381.PAIR;
import org.apache.milagro.amcl.BLS381.ROM;
import org.apache.milagro.amcl.RAND;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;

import static org.ethereum.sharding.crypto.BLS381Sign.ECP2_POINT_SIZE;
import static org.ethereum.sharding.crypto.BLS381Sign.ECP_POINT_SIZE;
import static org.ethereum.sharding.crypto.BLS381Sign.INT_SIZE;

public class MilagroBLS381 implements BLS381 {

    public static BIG CURVE_ORDER = new BIG(ROM.CURVE_Order);

    private SecureRandom random = new SecureRandom();

    /**
     * Returns random scalar which is on a curve
     */
    @Override
    public BI generatePrivate() {
        RAND rand = new RAND();
        byte[] randomBytes = new byte[INT_SIZE];
        random.nextBytes(randomBytes);
        rand.seed(INT_SIZE, randomBytes);
        BIG randomNumber = BIG.randomnum(CURVE_ORDER, rand);

        return new MilagroBIG(randomNumber);
    }

    /**
     * Restores private key from standard {@link BigInteger}
     */
    @Override
    public BI restorePrivate(BigInteger value) {
        byte[] sigKeyBytes = ByteUtil.bigIntegerToBytes(value, INT_SIZE);

        return restorePrivate(sigKeyBytes);
    }

    /**
     * Restores private key from byte array
     */
    @Override
    public BI restorePrivate(byte[] value) {
        BIG sigKey = BIG.fromBytes(value);

        return new MilagroBIG(sigKey);
    }

    @Override
    public ECP1Point restoreECP1(byte[] value) {
        if (value == null || value.length != ECP_POINT_SIZE) {
            throw new RuntimeException(String.format("Supports only %s size byte[] input", ECP_POINT_SIZE));
        }

        return new MilagroECP1(ECP.fromBytes(value));
    }

    @Override
    public ECP2Point restoreECP2(byte[] value) {
        if (value == null || value.length != ECP2_POINT_SIZE) {
            throw new RuntimeException(String.format("Supports only %s size byte[] input", ECP2_POINT_SIZE));
        }

        return new MilagroECP2(ECP2.fromBytes(value));
    }

    /**
     * @return base point (generator) on ECP2
     */
    @Override
    public ECP2Point generator2() {
        return new MilagroECP2(ECP2.generator());
    }

    /**
     * Maps value to GroupG1 (ECP)
     */
    @Override
    public ECP1Point mapToECP1(byte[] value) {
        if (value == null || value.length != INT_SIZE) {
            throw new RuntimeException(String.format("Supports only %s size byte[] input", INT_SIZE));
        }

        return new MilagroECP1(ECP.mapit(value));
    }

    @Override
    public FP12Point pair(ECP2Point pointECP2, ECP1Point pointECP1) {
        if (!(pointECP2 instanceof MilagroECP2) || !(pointECP1 instanceof MilagroECP1)) {
            throw new RuntimeException("Supports only Milagro format of ECP2 and ECP1");
        }
        MilagroECP2 ecp2Point = (MilagroECP2) pointECP2;
        MilagroECP1 ecp1Point = (MilagroECP1) pointECP1;

        FP12 p = PAIR.ate(ecp2Point.value, ecp1Point.value);
        FP12 res = PAIR.fexp(p);

        return new MilagroFP12(res);
    }

    class MilagroECP1 implements ECP1Point {
        ECP value;

        public MilagroECP1(ECP value) {
            this.value = value;
        }

        @Override
        public void add(ECP1Point value) {
            if (!(value instanceof MilagroECP1)) {
                throw new RuntimeException("Supports only Milagro format of ECP1 point");
            }

            MilagroECP1 multiplier = (MilagroECP1) value;
            this.value.add(multiplier.value);
        }

        @Override
        public ECP1Point mul(BI value) {
            if (!(value instanceof MilagroBIG)) {
                throw new RuntimeException("Supports only Milagro format of BigInteger");
            }

            MilagroBIG scalar = (MilagroBIG) value;
            return new MilagroECP1(this.value.mul(scalar.value));
        }

        @Override
        public BigInteger asBigInteger() {
            return new BigInteger(asByteArray());
        }

        @Override
        public byte[] asByteArray() {
            byte[] res = new byte[ECP_POINT_SIZE];
            value.toBytes(res, false);

            return res;
        }
    }

    class MilagroECP2 implements ECP2Point {
        ECP2 value;

        public MilagroECP2(ECP2 value) {
            this.value = value;
        }

        @Override
        public void add(ECP2Point value) {
            if (!(value instanceof MilagroECP2)) {
                throw new RuntimeException("Supports only Milagro format of ECP2 point");
            }

            MilagroECP2 multiplier = (MilagroECP2) value;
            this.value.add(multiplier.value);
        }

        @Override
        public ECP2Point mul(BI value) {
            if (!(value instanceof MilagroBIG)) {
                throw new RuntimeException("Supports only Milagro format of BigInteger");
            }

            MilagroBIG scalar = (MilagroBIG) value;
            return new MilagroECP2(this.value.mul(scalar.value));
        }

        @Override
        public BigInteger asBigInteger() {
            return new BigInteger(asByteArray());
        }

        @Override
        public byte[] asByteArray() {
            byte[] res = new byte[ECP2_POINT_SIZE];
            value.toBytes(res);

            return res;
        }
    }

    class MilagroBIG implements BI {
        BIG value;

        public MilagroBIG(BIG value) {
            this.value = value;
        }

        @Override
        public BigInteger asBigInteger() {
            return new BigInteger(asByteArray());
        }

        @Override
        public byte[] asByteArray() {
            byte[] res = new byte[INT_SIZE];
            value.toBytes(res);

            return res;
        }
    }

    class MilagroFP12 implements FP12Point {
        FP12 value;

        public MilagroFP12(FP12 value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MilagroFP12 that = (MilagroFP12) o;
            return value.equals(that.value);
        }
    }
}

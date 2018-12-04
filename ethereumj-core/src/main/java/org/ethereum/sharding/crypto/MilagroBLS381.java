package org.ethereum.sharding.crypto;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.milagro.amcl.BLS381.PAIR;
import org.apache.milagro.amcl.BLS381.ROM;
import org.apache.milagro.amcl.RAND;

import java.security.SecureRandom;

import static org.ethereum.sharding.crypto.BLS381Sign.ECP2_POINT_SIZE;
import static org.ethereum.sharding.crypto.BLS381Sign.ECP_POINT_SIZE;
import static org.ethereum.sharding.crypto.BLS381Sign.SCALAR_SIZE;

public class MilagroBLS381 implements BLS381 {

    public static BIG CURVE_ORDER = new BIG(ROM.CURVE_Order);

    private SecureRandom random = new SecureRandom();

    /**
     * Returns random scalar which is on a curve
     */
    @Override
    public Scalar generateRandomPrivate() {
        RAND rand = new RAND();
        byte[] randomBytes = new byte[SCALAR_SIZE];
        random.nextBytes(randomBytes);
        rand.seed(SCALAR_SIZE, randomBytes);
        BIG randomNumber = BIG.randomnum(CURVE_ORDER, rand);

        return new MilagroBIG(randomNumber);
    }

    /**
     * Restores private key from byte array
     */
    @Override
    public Scalar restoreScalar(byte[] value) {
        BIG sigKey = BIG.fromBytes(value);

        return new MilagroBIG(sigKey);
    }

    @Override
    public P1 restoreECP1(byte[] value) {
        if (value == null || value.length != ECP_POINT_SIZE) {
            throw new RuntimeException(String.format("Supports only %s size byte[] input", ECP_POINT_SIZE));
        }

        return new MilagroECP1(ECP.fromBytes(value));
    }

    @Override
    public P2 restoreECP2(byte[] value) {
        if (value == null || value.length != ECP2_POINT_SIZE) {
            throw new RuntimeException(String.format("Supports only %s size byte[] input", ECP2_POINT_SIZE));
        }

        return new MilagroECP2(ECP2.fromBytes(value));
    }

    /**
     * @return base point (generator) on ECP1
     */
    @Override
    public P1 generator() {
        return new MilagroECP1(ECP.generator());
    }

    /**
     * Maps value to GroupG2 (ECP2)
     */
    @Override
    public P2 mapToECP2(byte[] value) {
        if (value == null || value.length != SCALAR_SIZE) {
            throw new RuntimeException(String.format("Supports only %s size byte[] input", SCALAR_SIZE));
        }

        return new MilagroECP2(ECP2.mapit(value));
    }

    @Override
    public FP12 pair(P2 point2, P1 point1) {
        if (!(point2 instanceof MilagroECP2) || !(point1 instanceof MilagroECP1)) {
            throw new RuntimeException("Supports only Milagro format of ECP2 and ECP1");
        }
        MilagroECP2 ecp2Point = (MilagroECP2) point2;
        MilagroECP1 ecp1Point = (MilagroECP1) point1;

        org.apache.milagro.amcl.BLS381.FP12 p = PAIR.ate(ecp2Point.value, ecp1Point.value);
        org.apache.milagro.amcl.BLS381.FP12 res = PAIR.fexp(p);

        return new MilagroFP12(res);
    }

    class MilagroECP1 implements P1 {
        ECP value;

        public MilagroECP1(ECP value) {
            this.value = value;
        }

        @Override
        public void add(P1 value) {
            if (!(value instanceof MilagroECP1)) {
                throw new RuntimeException("Supports only Milagro format of ECP1 point");
            }

            MilagroECP1 multiplier = (MilagroECP1) value;
            this.value.add(multiplier.value);
        }

        @Override
        public P1 mul(Scalar value) {
            if (!(value instanceof MilagroBIG)) {
                throw new RuntimeException("Supports only Milagro format of BigInteger");
            }

            MilagroBIG scalar = (MilagroBIG) value;
            return new MilagroECP1(this.value.mul(scalar.value));
        }

        @Override
        public byte[] asByteArray() {
            byte[] res = new byte[ECP_POINT_SIZE];
            value.toBytes(res, false);

            return res;
        }
    }

    class MilagroECP2 implements P2 {
        ECP2 value;

        public MilagroECP2(ECP2 value) {
            this.value = value;
        }

        @Override
        public void add(P2 value) {
            if (!(value instanceof MilagroECP2)) {
                throw new RuntimeException("Supports only Milagro format of ECP2 point");
            }

            MilagroECP2 multiplier = (MilagroECP2) value;
            this.value.add(multiplier.value);
        }

        @Override
        public P2 mul(Scalar value) {
            if (!(value instanceof MilagroBIG)) {
                throw new RuntimeException("Supports only Milagro format of BigInteger");
            }

            MilagroBIG scalar = (MilagroBIG) value;
            return new MilagroECP2(this.value.mul(scalar.value));
        }

        @Override
        public byte[] asByteArray() {
            byte[] res = new byte[ECP2_POINT_SIZE];
            value.toBytes(res);

            return res;
        }
    }

    class MilagroBIG implements Scalar {
        BIG value;

        public MilagroBIG(BIG value) {
            this.value = value;
        }

        @Override
        public byte[] asByteArray() {
            byte[] res = new byte[SCALAR_SIZE];
            value.toBytes(res);

            return res;
        }
    }

    class MilagroFP12 implements FP12 {
        org.apache.milagro.amcl.BLS381.FP12 value;

        public MilagroFP12(org.apache.milagro.amcl.BLS381.FP12 value) {
            this.value = value;
        }

        @Override
        public boolean equals(FP12 other) {
            return equals((Object) other);
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

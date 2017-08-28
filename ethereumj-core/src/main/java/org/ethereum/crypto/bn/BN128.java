package org.ethereum.crypto.bn;

import java.math.BigInteger;

/**
 * Implementation of Barreto–Naehrig curve which is applicable for zkSNARKs calculations. <br/>
 * This specific curve was introduced in
 * <a href="https://github.com/scipr-lab/libff#elliptic-curve-choices">libff</a>
 * and used by a proving system in
 * <a href="https://github.com/zcash/zcash/wiki/specification#zcash-protocol">ZCash protocol</a> <br/>
 * <br/>
 *
 * Curve equation: <br/>
 * Y^2 = X^3 + b, where "b" equals 3 <br/>
 * The curve is defined over F_p, where "p" equals 21888242871839275222246405745257275088696311157297823662689037894645226208583 <br/>
 * Point at infinity is encoded as <code>(0, 0)</code> <br/>
 * <br/>
 *
 * Code of {@link #add(BN128)} and {@link #dbl()} has been ported from
 * <a href="https://github.com/scipr-lab/libff/blob/master/libff/algebra/curves/alt_bn128/alt_bn128_g1.cpp">libff</a>
 *
 * @author Mikhail Kalinin
 * @since 21.08.2017
 */
public class BN128 {

    // "b" curve parameter
    private static final BigInteger B = BigInteger.valueOf(3);

    // "p" field parameter
    private static final BigInteger P = new BigInteger("21888242871839275222246405745257275088696311157297823662689037894645226208583");

    // the point at infinity
    private static final BN128 ZERO = new BN128(BigInteger.ZERO, BigInteger.ZERO);

    /**
     * Convenient calculations modulo P
     */
    static class Fp {

        static final Fp ONE = new Fp(BigInteger.ONE);

        BigInteger v;

        Fp(BigInteger v) { this.v = v; }
        Fp add(Fp o) { return new Fp(this.v.add(o.v).mod(P)); }
        Fp add(BigInteger o) { return new Fp(this.v.add(o).mod(P)); }
        Fp mul(Fp o) { return new Fp(this.v.multiply(o.v).mod(P)); }
        Fp sub(Fp o) { return new Fp(this.v.subtract(o.v).mod(P)); }
        Fp squared() { return new Fp(v.multiply(v).mod(P)); }
        Fp dbl() { return new Fp(v.add(v).mod(P)); }
        Fp inverse() { return new Fp(v.modInverse(P)); }
        boolean isZero() { return v.compareTo(BigInteger.ZERO) == 0; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Fp fp = (Fp) o;

            return !(v != null ? v.compareTo(fp.v) != 0 : fp.v != null);
        }

        @Override
        public String toString() {
            return v.toString();
        }
    }

    private Fp x;
    private Fp y;

    private BN128(Fp x, Fp y) {
        this.x = x;
        this.y = y;
    }

    private BN128(BigInteger x, BigInteger y) {
        this.x = new Fp(x);
        this.y = new Fp(y);
    }

    /**
     * Checks whether x and y belong to Fp,
     * then checks whether point with (x; y) coordinates lays on the curve.
     *
     * Returns new point if all checks have been passed,
     * otherwise returns null
     */
    public static BN128 create(byte[] xx, byte[] yy) {

        BigInteger bx = new BigInteger(1, xx);
        BigInteger by = new BigInteger(1, yy);

        // check whether scalars belongs to F_p
        if (bx.compareTo(P) >= 0) return null;
        if (by.compareTo(P) >= 0) return null;

        Fp x = new Fp(bx);
        Fp y = new Fp(by);

        // check for point at infinity
        if (x.isZero() && y.isZero()) {
            return ZERO;
        }

        // check whether point belongs to the curve
        if (!isOnCurve(x, y)) return null;

        return new BN128(x, y);
    }

    /**
     * Transforms given Jacobian to affine coordinates and then creates a point
     */
    static BN128 fromJacobian(Fp x, Fp y, Fp z) {

        Fp zInv = z.inverse();
        Fp zInv2 = zInv.squared();
        Fp zInv3 = zInv2.mul(zInv);

        Fp ax = x.mul(zInv2);
        Fp ay = y.mul(zInv3);

        return new BN128(ax, ay);
    }

    private static boolean isOnCurve(Fp x, Fp y) {

        if (x.isZero() && y.isZero()) return true;

        Fp left  = y.squared();               // y^2
        Fp right = x.squared().mul(x).add(B); // x^3 + 3
        return left.equals(right);
    }

    public BN128 add(BN128 o) {

        if (this.isZero()) return o; // 0 + P = P
        if (o.isZero()) return this; // P + 0 = P

        Fp x1 = this.x, y1 = this.y;
        Fp x2 = o.x,    y2 = o.y;

        if (x1.equals(x2))
            if (y1.equals(y2)) {
                return this.dbl(); // P + P = 2P
            } else {
                return ZERO;        // P + (-P) = 0
            }

        // ported code is started from here
        // next calculations are done in Jacobian coordinates assuming that z1 = 1, z2 = 1

        Fp h = x2.sub(x1);          // h = x2 - x1
        Fp i = h.dbl().squared();   // i = (2 * h)^2
        Fp j = h.mul(i);            // j = h * i
        Fp r = y2.sub(y1).dbl();    // r = 2 * (y2 - y1)
        Fp v = x1.mul(i);           // v = x1 * i

        Fp x3 = r.squared().sub(j).sub(v.dbl());        // x3 = r^2 - j - 2 * v
        Fp y3 = v.sub(x3).mul(r).sub(y1.mul(j).dbl());  // y3 = r * (v - x3) - 2 * (y1 * j)
        Fp z3 = ZZ.mul(h); // z3 = ((z1+z2)^2 - z1^2 - z2^2) * h = ZZ * h

        return fromJacobian(x3, y3, z3);
    }
    // zz = ((z1+z2)^2 - z1^2 - z2^2), z1 and z2 always equal 1
    static final Fp ZZ = Fp.ONE.add(Fp.ONE).squared().sub(Fp.ONE).sub(Fp.ONE);


    public BN128 mul(BigInteger s) {

        if (s.compareTo(BigInteger.ZERO) == 0) // P * 0 = 0
            return ZERO;

        if (isZero()) return this; // 0 * s = 0

        // keep s immutable
        BN128 res = ZERO;
        BN128 addend = this;

        while (s.compareTo(BigInteger.ZERO) != 0) {

            if (s.testBit(0))  // add if bit is set
                res = res.add(addend);

            s = s.shiftRight(1);

            if (s.compareTo(BigInteger.ZERO) != 0) // double if m still has non-zero bits
                addend = addend.dbl();
        }

        return res;
    }

    private BN128 dbl() {

        if (isZero()) return this;

        // ported code is started from here
        // next calculations are done in Jacobian coordinates with z = 1

        Fp a = x.squared();     // a = x^2
        Fp b = y.squared();     // b = y^2
        Fp c = b.squared();     // c = b^2
        Fp d = x.add(b).squared().sub(a).sub(c);
        d = d.add(d);                              // d = 2 * ((x + b)^2 - a - c)
        Fp e = a.add(a).add(a);  // e = 3 * a
        Fp f = e.squared();     // f = e^2

        Fp x3 = f.sub(d.add(d)); // rx = f - 2 * d
        Fp y3 = e.mul(d.sub(x3)).sub(c.dbl().dbl().dbl()); // ry = e * (d - rx) - 8 * c
        Fp z3 = y.dbl(); // z3 = 2 * y * z = 2 * y

        return fromJacobian(x3, y3, z3);
    }

    public byte[] xBytes() {
        return x.v.toByteArray();
    }

    public byte[] yBytes() {
        return y.v.toByteArray();
    }

    public boolean isZero() {
        return x.isZero() && y.isZero();
    }

    @Override
    public String toString() {
        return String.format("(%s; %s)", x.v.toString(), y.v.toString());
    }
}

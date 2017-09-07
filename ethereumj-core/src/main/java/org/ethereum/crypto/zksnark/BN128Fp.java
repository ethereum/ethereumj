package org.ethereum.crypto.zksnark;

import static org.ethereum.crypto.zksnark.Params.B_Fp;

/**
 * Definition of {@link BN128} over F_p, where "p" equals {@link Params#P} <br/>
 *
 * Curve equation: <br/>
 * Y^2 = X^3 + b, where "b" equals {@link Params#B_Fp} <br/>
 *
 * @author Mikhail Kalinin
 * @since 21.08.2017
 */
public class BN128Fp extends BN128<Fp> {

    // the point at infinity
    static final BN128<Fp> ZERO = new BN128Fp(Fp.ZERO, Fp.ZERO, Fp.ZERO);

    protected BN128Fp(Fp x, Fp y, Fp z) {
        super(x, y, z);
    }

    @Override
    protected BN128<Fp> zero() {
        return ZERO;
    }

    @Override
    protected BN128<Fp> instance(Fp x, Fp y, Fp z) {
        return new BN128Fp(x, y, z);
    }

    @Override
    protected Fp b() {
        return B_Fp;
    }

    @Override
    protected Fp one() {
        return Fp._1;
    }

    /**
     * Checks whether x and y belong to Fp,
     * then checks whether point with (x; y) coordinates lays on the curve.
     *
     * Returns new point if all checks have been passed,
     * otherwise returns null
     */
    public static BN128<Fp> create(byte[] xx, byte[] yy) {

        Fp x = Fp.create(xx);
        Fp y = Fp.create(yy);

        // check for point at infinity
        if (x.isZero() && y.isZero()) {
            return ZERO;
        }

        BN128<Fp> p = new BN128Fp(x, y, Fp._1);

        // check whether point is a valid one
        if (p.isValid()) {
            return p;
        } else {
            return null;
        }
    }
}

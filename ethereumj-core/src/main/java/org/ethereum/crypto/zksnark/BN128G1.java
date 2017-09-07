package org.ethereum.crypto.zksnark;

/**
 * Implementation of specific cyclic subgroup of points belonging to {@link BN128Fp} <br/>
 * Members of this subgroup are passed as a first param to pairing input {@link PairingCheck#addPair(BN128G1, BN128G2)} <br/>
 *
 * Subgroup generator G = (1; 2)
 *
 * @author Mikhail Kalinin
 * @since 01.09.2017
 */
public class BN128G1 extends BN128Fp {

    BN128G1(BN128<Fp> p) {
        super(p.x, p.y, p.z);
    }

    @Override
    public BN128G1 toAffine() {
        return new BN128G1(super.toAffine());
    }

    /**
     * Checks whether point is a member of subgroup,
     * returns a point if check has been passed and null otherwise
     */
    public static BN128G1 create(byte[] x, byte[] y) {

        BN128<Fp> p = BN128Fp.create(x, y);

        if (p == null) return null;

        if (!isGroupMember(p)) return null;

        return new BN128G1(p);
    }

    /**
     * Formally we have to do this check
     * but in our domain it's not necessary,
     * thus always return true
     */
    private static boolean isGroupMember(BN128<Fp> p) {
        return true;
    }
}

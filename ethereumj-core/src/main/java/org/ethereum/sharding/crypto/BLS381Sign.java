package org.ethereum.sharding.crypto;

import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.ethereum.sharding.crypto.BLS381.Scalar;
import static org.ethereum.sharding.crypto.BLS381.P1;
import static org.ethereum.sharding.crypto.BLS381.P2;
import static org.ethereum.sharding.crypto.BLS381.FP12;

/**
 *  This is an implementation of signature creation, verification and
 *  aggregation based on the BLS12-381 pairing-friendly elliptic curve.
 *
 *  For curve parameters read original zkcrypto docs:
 *  https://github.com/zkcrypto/pairing/tree/master/src/bls12_381/
 *  Why this curve was chosen:
 *  https://z.cash/blog/new-snark-curve/
 *  Eth 2.0 specific implementation
 *  https://github.com/ethereum/eth2.0-specs/blob/master/specs/bls_verify.md
 */
public class BLS381Sign implements Sign {

    public static int SCALAR_SIZE = 48;

    public static int ECP_POINT_SIZE = 2 * SCALAR_SIZE + 1;

    public static int ECP2_POINT_SIZE = 4 * SCALAR_SIZE;

    public static byte[] DOMAIN = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

    //  Milagro implementation of BLS12-381 curve is used underneath.
    private BLS381 bls381 = new MilagroBLS381();

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    public KeyPair newKeyPair() {
        Scalar sigKey = bls381.generateRandomPrivate();
        P1 verKey = bls381.generator().mul(sigKey);

        KeyPair res = new KeyPair();
        res.sigKey = new BigInteger(sigKey.asByteArray());
        res.verKey = new BigInteger(verKey.asByteArray());

        return res;
    }

    /**
     * Derives public key from private
     */
    @Override
    public BigInteger privToPub(BigInteger privKey) {
        Scalar sigKey = bls381.restoreScalar(ByteUtil.bigIntegerToBytes(privKey, SCALAR_SIZE));
        P1 verKey = bls381.generator().mul(sigKey);

        return new BigInteger(verKey.asByteArray());
    }

    /**
     * Signs the message using its hash
     * @param msgHash       Message hash, expects 384 bits
     * @param domain        Implementation specific byte value which "salts"
     *                          the way msgHash is mapped to EC2
     * @param privateKey    Private key
     * @return  signature, point on G2 (bytes)
     */
    @Override
    public byte[] sign(byte[] msgHash, byte[] domain, BigInteger privateKey) {
        P2 hashPointECP2 = mapToECP2(msgHash, domain);
        P2 signature = hashPointECP2.mul(bls381.restoreScalar(ByteUtil.bigIntegerToBytes(privateKey, SCALAR_SIZE)));

        return signature.asByteArray();
    }

    /**
     * Verifies 384-bit hash and signature sig using Verification (Public)
     * key verKey. Returns true if verification succeeded.
     * @param signature     Signature, G2 point
     * @param msgHash       Message hash
     * @param domain        Implementation specific byte value which "salts"
     *                          the way msgHash is mapped to EC2
     * @param publicKey     Verification key, G1 point
     * @return  true if message is signature is done with the key
     */
    @Override
    public boolean verify(byte[] signature, byte[] msgHash, BigInteger publicKey, byte[] domain) {
        // signature to ECP2, publicKey to ECP
        byte[] verKeyBytes = ByteUtil.bigIntegerToBytes(publicKey, ECP_POINT_SIZE);
        P2 sigPoint = bls381.restoreECP2(signature);
        P1 publicKeyPoint = bls381.restoreECP1(verKeyBytes);

        P1 generator = bls381.generator();
        // TODO: domain-specific tests
        P2 point = mapToECP2(msgHash, domain);

        FP12 lhs = bls381.pair(sigPoint, generator);
        FP12 rhs = bls381.pair(point, publicKeyPoint);

        return lhs.equals(rhs);
    }

    private P2 mapToECP2(byte[] msgHash, byte[] domain) {
        // TODO: implement me
//        x1 = hash(bytes8(domain) + b'\x01' + m)
//        x2 = hash(bytes8(domain) + b'\x02' + m)
//        x_coord = FQ2([x1, x2]) # x1 + x2 * i
//        while 1:
//        x_cubed_plus_b2 = x_coord ** 3 + FQ2([4,4])
//        y_coord = mod_sqrt(x_cubed_plus_b2)
//        if y_coord is not None:
//        break
//                x_coord += FQ2([1, 0]) # Add one until we get a quadratic residue
//        assert is_on_curve((x_coord, y_coord))
//        return multiply((x_coord, y_coord), G2_cofactor)

//        qmod = field_modulus ** 2 - 1
//        eighth_roots_of_unity = [FQ2([1,1]) ** ((qmod * k) // 8) for k in range(8)]
//
//        def mod_sqrt(val):
//        candidate_sqrt = val ** ((qmod + 8) // 16)
//        check = candidate_sqrt ** 2 / val
//        if check in eighth_roots_of_unity[::2]:
//            return candidate_sqrt / eighth_roots_of_unity[eighth_roots_of_unity.index(check) // 2]
//        return None
        return bls381.mapToECP2(msgHash);
    }

    /**
     * Aggregates several signatures in one
     */
    @Override
    public byte[] aggSigns(List<byte[]> signatures) {
        List<P2> sigs = signatures.stream()
                .map((byte[] signature) -> bls381.restoreECP2(signature))
                .collect(Collectors.toList());

        P2 g2Agg = null;
        for(P2 sig: sigs) {
            if (g2Agg == null) {
                g2Agg = sig;
            } else {
                g2Agg.add(sig);
            }
        }

        return g2Agg.asByteArray();
    }

    /**
     * Aggregates public keys
     */
    @Override
    public BigInteger aggPubs(List<BigInteger> verificationKeys) {
        List<P1> verKeys = verificationKeys.stream()
                .map((BigInteger b) -> bls381.restoreECP1(ByteUtil.bigIntegerToBytes(b, ECP_POINT_SIZE)))
                .collect(Collectors.toList());

        P1 g1Agg = null;

        for(P1 ver: verKeys) {
            if (g1Agg == null) {
                g1Agg = ver;
            } else {
                g1Agg.add(ver);
            }
        }

        return new BigInteger(g1Agg.asByteArray());
    }
}

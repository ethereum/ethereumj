package org.ethereum.sharding.crypto;

import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.ethereum.sharding.crypto.BLS381.BI;
import static org.ethereum.sharding.crypto.BLS381.ECP1Point;
import static org.ethereum.sharding.crypto.BLS381.ECP2Point;
import static org.ethereum.sharding.crypto.BLS381.FP12Point;

/**
 *  This is an implementation of signature creation, verification and
 *  aggregation based on the BLS12-381 pairing-friendly elliptic curve.
 *
 *  For curve parameters read original zkcrypto docs:
 *  https://github.com/zkcrypto/pairing/tree/master/src/bls12_381/
 *  Why this curve was chosen:
 *  https://z.cash/blog/new-snark-curve/
 */
public class BLS381Sign implements Sign {

    public static int INT_SIZE = 48;

    public static int ECP_POINT_SIZE = 2 * INT_SIZE + 1;

    public static int ECP2_POINT_SIZE = 4 * INT_SIZE;

    //  Milagro implementation of BLS12-381 curve is used underneath.
    private BLS381 bls381 = new MilagroBLS381();

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    public KeyPair newKeyPair() {
        BI sigKey = bls381.generatePrivate();
        ECP2Point verKey = bls381.generator2().mul(sigKey);

        KeyPair res = new KeyPair();
        res.sigKey = sigKey.asBigInteger();
        res.verKey = verKey.asBigInteger();

        return res;
    }

    /**
     * Derives public key from private
     */
    @Override
    public BigInteger privToPub(BigInteger privKey) {
        BI sigKey = bls381.restorePrivate(privKey);
        ECP2Point verKey = bls381.generator2().mul(sigKey);

        return verKey.asBigInteger();
    }

    /**
     * Signs the message using its hash
     * @param privateKey    Private key
     * @param msgHash       Message hash, expects 384 bits
     * @return  signature, point on G1 (bytes)
     */
    @Override
    public Signature sign(byte[] msgHash, BigInteger privateKey) {
        ECP1Point hashPointECP1 = bls381.mapToECP1(msgHash);
        ECP1Point signature = hashPointECP1.mul(bls381.restorePrivate(privateKey));

        return new Signature(signature.asBigInteger());
    }

    /**
     * Verifies 384-bit hash and signature sig using Verification (Public)
     * key verKey. Returns true if verification succeeded.
     * @param signature     Signature, G1 point
     * @param msgHash       Message hash
     * @param publicKey     Verification key, G2 point
     * @return  true if message is signature is done with the key
     */
    @Override
    public boolean verify(Signature signature, byte[] msgHash, BigInteger publicKey) {
        // signature to ECP2, publicKey to ECP
        byte[] verKeyBytes = ByteUtil.bigIntegerToBytes(publicKey, ECP2_POINT_SIZE);
        byte[] sigKeyBytes = ByteUtil.bigIntegerToBytes(signature.value, ECP_POINT_SIZE);

        ECP1Point sigPoint = bls381.restoreECP1(sigKeyBytes);
        ECP2Point publicKeyPoint = bls381.restoreECP2(verKeyBytes);

        ECP2Point generator = bls381.generator2();
        ECP1Point point = bls381.mapToECP1(msgHash);

        FP12Point lhs = bls381.pair(generator, sigPoint);
        FP12Point rhs = bls381.pair(publicKeyPoint, point);

        return lhs.equals(rhs);
    }

    /**
     * Aggregates several signatures in one
     */
    @Override
    public Signature aggSigns(List<Signature> signatures) {
        List<ECP1Point> sigs = signatures.stream()
                .map((Signature s) -> bls381.restoreECP1(ByteUtil.bigIntegerToBytes(s.value, ECP_POINT_SIZE)))
                .collect(Collectors.toList());

        ECP1Point g1Agg = null;
        for(ECP1Point sig: sigs) {
            if (g1Agg == null) {
                g1Agg = sig;
            } else {
                g1Agg.add(sig);
            }
        }

        return new Signature(g1Agg.asBigInteger());
    }

    /**
     * Aggregates public keys
     */
    @Override
    public BigInteger aggPubs(List<BigInteger> verificationKeys) {
        List<ECP2Point> verKeys = verificationKeys.stream()
                .map((BigInteger b) -> bls381.restoreECP2(ByteUtil.bigIntegerToBytes(b, ECP2_POINT_SIZE)))
                .collect(Collectors.toList());

        ECP2Point g2Agg = null;

        for(ECP2Point ver: verKeys) {
            if (g2Agg == null) {
                g2Agg = ver;
            } else {
                g2Agg.add(ver);
            }
        }

        return g2Agg.asBigInteger();
    }
}

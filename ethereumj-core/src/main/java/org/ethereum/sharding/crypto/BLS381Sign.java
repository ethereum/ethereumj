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
 */
public class BLS381Sign implements Sign {

    public static int SCALAR_SIZE = 48;

    public static int ECP_POINT_SIZE = 2 * SCALAR_SIZE + 1;

    public static int ECP2_POINT_SIZE = 4 * SCALAR_SIZE;

    //  Milagro implementation of BLS12-381 curve is used underneath.
    private BLS381 bls381 = new MilagroBLS381();

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    public KeyPair newKeyPair() {
        Scalar sigKey = bls381.generateRandomPrivate();
        P2 verKey = bls381.generator2().mul(sigKey);

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
        P2 verKey = bls381.generator2().mul(sigKey);

        return new BigInteger(verKey.asByteArray());
    }

    /**
     * Signs the message using its hash
     * @param privateKey    Private key
     * @param msgHash       Message hash, expects 384 bits
     * @return  signature, point on G1 (bytes)
     */
    @Override
    public byte[] sign(byte[] msgHash, BigInteger privateKey) {
        P1 hashPointECP1 = bls381.mapToECP1(msgHash);
        P1 signature = hashPointECP1.mul(bls381.restoreScalar(ByteUtil.bigIntegerToBytes(privateKey, SCALAR_SIZE)));

        return signature.asByteArray();
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
    public boolean verify(byte[] signature, byte[] msgHash, BigInteger publicKey) {
        // signature to ECP2, publicKey to ECP
        byte[] verKeyBytes = ByteUtil.bigIntegerToBytes(publicKey, ECP2_POINT_SIZE);
        P1 sigPoint = bls381.restoreECP1(signature);
        P2 publicKeyPoint = bls381.restoreECP2(verKeyBytes);

        P2 generator = bls381.generator2();
        P1 point = bls381.mapToECP1(msgHash);

        FP12 lhs = bls381.pair(generator, sigPoint);
        FP12 rhs = bls381.pair(publicKeyPoint, point);

        return lhs.equals(rhs);
    }

    /**
     * Aggregates several signatures in one
     */
    @Override
    public byte[] aggSigns(List<byte[]> signatures) {
        List<P1> sigs = signatures.stream()
                .map((byte[] signature) -> bls381.restoreECP1(signature))
                .collect(Collectors.toList());

        P1 g1Agg = null;
        for(P1 sig: sigs) {
            if (g1Agg == null) {
                g1Agg = sig;
            } else {
                g1Agg.add(sig);
            }
        }

        return g1Agg.asByteArray();
    }

    /**
     * Aggregates public keys
     */
    @Override
    public BigInteger aggPubs(List<BigInteger> verificationKeys) {
        List<P2> verKeys = verificationKeys.stream()
                .map((BigInteger b) -> bls381.restoreECP2(ByteUtil.bigIntegerToBytes(b, ECP2_POINT_SIZE)))
                .collect(Collectors.toList());

        P2 g2Agg = null;

        for(P2 ver: verKeys) {
            if (g2Agg == null) {
                g2Agg = ver;
            } else {
                g2Agg.add(ver);
            }
        }

        return new BigInteger(g2Agg.asByteArray());
    }
}

package org.ethereum.sharding.crypto;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.List;

import static org.ethereum.sharding.crypto.MilagroBLS381.ECP2_POINT_SIZE;
import static org.ethereum.sharding.crypto.MilagroBLS381.ECP_POINT_SIZE;
import static org.ethereum.sharding.crypto.MilagroBLS381.PRIVATE_SIZE;

/**
 *  This is an implementation of signature creation, verification and
 *  aggregation based on the BLS12-381 pairing-friendly elliptic curve.
 *
 *  For curve parameters read original zkcrypto docs:
 *  https://github.com/zkcrypto/pairing/tree/master/src/bls12_381/
 *  Why this curve was chosen:
 *  https://z.cash/blog/new-snark-curve/
 *
 *  Milagro implementation {@link MilagroBLS381} of BLS12-381 curve is used underneath.
 */
public class BLS381Sign implements Sign {

    MilagroBLS381 milagro = new MilagroBLS381();

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    public KeyPair newKeyPair() {
        MilagroBLS381.KeyPair keyPair = milagro.newKeyPair();
        byte[] sigKey = new byte[PRIVATE_SIZE];
        keyPair.sigKey.toBytes(sigKey);
        byte[] verKey = ecp2ToBytes(keyPair.verKey);

        KeyPair res = new KeyPair();
        res.sigKey = new BigInteger(sigKey);
        res.verKey = new BigInteger(verKey);

        return res;
    }

    /**
     * Derives public key from private
     */
    @Override
    public BigInteger privToPub(BigInteger privKey) {
        byte[] sigKeyBytes = ByteUtil.bigIntegerToBytes(privKey, PRIVATE_SIZE);
        BIG sigKey = BIG.fromBytes(sigKeyBytes);
        byte[] verKey = ecp2ToBytes(milagro.fromSigKey(sigKey));

        return new BigInteger(verKey);
    }

    private byte[] ecp2ToBytes(ECP2 point) {
        byte[] res = new byte[ECP2_POINT_SIZE];
        point.toBytes(res);

        return res;
    }

    /**
     * Signs the message using its hash
     * @param privateKey    Private key
     * @param msgHash       Message hash, expects 384 bits
     * @return  signature, point on G1 (bytes)
     */
    @Override
    public Signature sign(byte[] msgHash, BigInteger privateKey) {
        byte[] sigKeyBytes = ByteUtil.bigIntegerToBytes(privateKey, PRIVATE_SIZE);
        ECP signPoint = milagro.signMessage(BIG.fromBytes(sigKeyBytes), msgHash);

        return new Signature(new BigInteger(ecpToBytes(signPoint)));
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

        return milagro.verifyMessage(ECP.fromBytes(sigKeyBytes), msgHash, ECP2.fromBytes(verKeyBytes));
    }

    /**
     * Aggregates several signatures in one
     */
    @Override
    public Signature aggSigns(List<Signature> signatures) {
        ECP[] sigs = signatures.stream()
                .map((Signature s) -> ECP.fromBytes(ByteUtil.bigIntegerToBytes(s.value, ECP_POINT_SIZE)))
                .toArray(ECP[]::new);
        ECP combinedSig = milagro.combine(sigs);
        BigInteger sig = new BigInteger(ecpToBytes(combinedSig));

        return new Signature(sig);
    }

    /**
     * Aggregates public keys
     */
    @Override
    public BigInteger aggPubs(List<BigInteger> verificationKeys) {
        ECP2[] verKeys = verificationKeys.stream()
                .map((BigInteger b) -> ECP2.fromBytes(ByteUtil.bigIntegerToBytes(b, ECP2_POINT_SIZE)))
                .toArray(ECP2[]::new);
        ECP2 combinedVerKey = milagro.combine(verKeys);

        return new BigInteger(ecp2ToBytes(combinedVerKey));
    }

    private byte[] ecpToBytes(ECP point) {
        byte[] res = new byte[ECP_POINT_SIZE];
        point.toBytes(res, false);

        return res;
    }
}

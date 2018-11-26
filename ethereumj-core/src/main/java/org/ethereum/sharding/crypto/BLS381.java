package org.ethereum.sharding.crypto;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.milagro.amcl.BLS381.FP12;
import org.apache.milagro.amcl.BLS381.PAIR;
import org.apache.milagro.amcl.BLS381.ROM;
import org.apache.milagro.amcl.RAND;

import java.security.SecureRandom;

import static org.apache.milagro.amcl.BLS381.BIG.MODBYTES;

public class BLS381 {

    public BIG CURVE_ORDER = new BIG(ROM.CURVE_Order);

    public int PRIVATE_SIZE = MODBYTES;

    public int ECP_POINT_SIZE = 2 * MODBYTES + 1;

    public int ECP2_POINT_SIZE = 4 * MODBYTES;

    private SecureRandom random = new SecureRandom();

    /**
     * Creates new random Signature (Private) key
     */
    public byte[] newSigKey() {
        byte[] res = new byte[PRIVATE_SIZE];
        RAND rand = new RAND();
        byte[] randomBytes = new byte[PRIVATE_SIZE];
        random.nextBytes(randomBytes);
        rand.seed(PRIVATE_SIZE, randomBytes);
        BIG randomNumber = BIG.randomnum(CURVE_ORDER, rand);
        randomNumber.toBytes(res);

        return res;
    }

    /**
     * Obtains Verification (Public) key
     * from Signature (Private) key on ECP2
     */
    public byte[] fromSigKey(BIG sigKey) {
        ECP2 point = generator2();
        return ecp2ToBytes(point.mul(sigKey));
    }

    private byte[] ecp2ToBytes(ECP2 point) {
        byte[] res = new byte[ECP2_POINT_SIZE];
        point.toBytes(res);

        return res;
    }

    private ECP2 generator2() {
        return ECP2.generator();
    }

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    public KeyPair newKeyPair() {
        KeyPair res = new KeyPair();
        res.sigKey = newSigKey();
        res.verKey = fromSigKey(BIG.fromBytes(res.sigKey));

        return res;
    }

    /**
     * Signs the message using its hash
     * @param sigKey    Private key
     * @param hash      Message hash, expects 384 bits
     * @return  signature, point on G1 (bytes)
     */
    public byte[] signMessage(byte[] sigKey, byte[] hash) {
        // Map hash value to GroupG1 (ECP)
        ECP point = ECP.mapit(hash);
        ECP g1 = point.mul(BIG.fromBytes(sigKey));

        return ecpToBytes(g1);
    }

    private byte[] ecpToBytes(ECP point) {
        byte[] res = new byte[ECP_POINT_SIZE];
        point.toBytes(res, false);

        return res;
    }

    /**
     * Verifies 384-bit hash and signature sig using Verification (Public)
     * key verKey. Returns true if verification succeeded.
     * @param sig       Signature, G1 point
     * @param hash      message hash
     * @param verKey    Verification key, G2 point
     * @return  true if message is signature is done with the key
     */
    public boolean verifyMessage(byte[] sig, byte[] hash, byte[] verKey) {
        ECP2 generator = generator2();
        ECP point = ECP.mapit(hash);
        FP12 lhs = atePairing(generator, ECP.fromBytes(sig));  // sig to ECP2
        FP12 rhs = atePairing(ECP2.fromBytes(verKey), point); // verKey to ECP

        return lhs.equals(rhs);
    }

    private FP12 atePairing(ECP2 point2, ECP point) {
        FP12 p = PAIR.ate(point2, point);
        return PAIR.fexp(p);
    }

    class KeyPair {
        byte[] sigKey;  // Signature (private key), point in GroupG1
        byte[] verKey;  // Verification (public key), point in GroupG2
    }
}

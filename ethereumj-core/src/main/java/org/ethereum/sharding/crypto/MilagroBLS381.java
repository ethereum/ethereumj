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

/**
 * Implementation uses internal {@link org.apache.milagro.amcl} types
 */
public class MilagroBLS381 {

    public static BIG CURVE_ORDER = new BIG(ROM.CURVE_Order);

    public static int PRIVATE_SIZE = MODBYTES;

    public static int ECP_POINT_SIZE = 2 * MODBYTES + 1;

    public static int ECP2_POINT_SIZE = 4 * MODBYTES;

    private SecureRandom random = new SecureRandom();

    /**
     * Creates new random Signature (Private) key
     */
    private BIG newSigKey() {
        RAND rand = new RAND();
        byte[] randomBytes = new byte[PRIVATE_SIZE];
        random.nextBytes(randomBytes);
        rand.seed(PRIVATE_SIZE, randomBytes);
        BIG randomNumber = BIG.randomnum(CURVE_ORDER, rand);

        return randomNumber;
    }

    /**
     * Obtains Verification (Public) key
     * from Signature (Private) key on ECP2
     */
    ECP2 fromSigKey(BIG sigKey) {
        ECP2 gen2 = generator2();
        return gen2.mul(sigKey);
    }

    private ECP2 generator2() {
        return ECP2.generator();
    }

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    KeyPair newKeyPair() {
        KeyPair res = new KeyPair();
        res.sigKey = newSigKey();
        res.verKey = fromSigKey(res.sigKey);

        return res;
    }

    /**
     * Signs the message using its hash
     * @param sigKey    Private key
     * @param hash      Message hash, expects 384 bits
     * @return  signature, point on G1 (bytes)
     */
    ECP signMessage(BIG sigKey, byte[] hash) {
        // Map hash value to GroupG1 (ECP)
        ECP hashG1 = ECP.mapit(hash);
        ECP point = hashG1.mul(sigKey);

        return point;
    }

    /**
     * Verifies 384-bit hash and signature sig using Verification (Public)
     * key verKey. Returns true if verification succeeded.
     * @param sig       Signature, G1 point
     * @param hash      message hash
     * @param verKey    Verification key, G2 point
     * @return  true if message is signature is done with the key
     */
    boolean verifyMessage(ECP sig, byte[] hash, ECP2 verKey) {
        ECP2 generator = generator2();
        ECP point = ECP.mapit(hash);
        FP12 lhs = atePairing(generator, sig);
        FP12 rhs = atePairing(verKey, point);

        return lhs.equals(rhs);
    }

    private FP12 atePairing(ECP2 point2, ECP point) {
        FP12 p = PAIR.ate(point2, point);
        return PAIR.fexp(p);
    }

    ECP combine(ECP... sigs) {
        ECP res = null;

        for(ECP sig: sigs) {
            if (res == null) {
                res = sig;
            } else {
                res.add(sig);
            }
        }

        return res;
    }

    ECP2 combine(ECP2... vers) {
        ECP2 res = null;

        for(ECP2 ver: vers) {
            if (res == null) {
                res = ver;
            } else {
                res.add(ver);
            }
        }

        return res;
    }

    class KeyPair {
        BIG sigKey;  // Signature (private key), point in GroupG1
        ECP2 verKey;  // Verification (public key), point in GroupG2
    }
}

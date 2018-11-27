package org.ethereum.sharding.crypto;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;

import static org.ethereum.sharding.crypto.BLS381Milagro.ECP2_POINT_SIZE;
import static org.ethereum.sharding.crypto.BLS381Milagro.PRIVATE_SIZE;

public class BLS381 {

    BLS381Milagro milagro = new BLS381Milagro();

    /**
     * Creates new random pair of Signature (Private) and
     * Verification (Public) keys
     */
    public KeyPair newKeyPair() {
        BLS381Milagro.KeyPair keyPair = milagro.newKeyPair();
        KeyPair res = new KeyPair();
        res.sigKey = new byte[PRIVATE_SIZE];
        keyPair.sigKey.toBytes(res.sigKey);
        res.verKey = ecp2ToBytes(keyPair.verKey);

        return res;
    }

    private byte[] ecp2ToBytes(ECP2 point) {
        byte[] res = new byte[ECP2_POINT_SIZE];
        point.toBytes(res);

        return res;
    }

    /**
     * Signs the message using its hash
     * @param sigKey    Private key
     * @param hash      Message hash, expects 384 bits
     * @return  signature, point on G1 (bytes)
     */
    public byte[] signMessage(byte[] sigKey, byte[] hash) {
        return milagro.signMessage(BIG.fromBytes(sigKey), hash);
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
        // sig to ECP2, verKey to ECP
        return milagro.verifyMessage(ECP.fromBytes(sig), hash, ECP2.fromBytes(verKey));
    }

    class KeyPair {
        byte[] sigKey;  // Signature (private key), point in GroupG1
        byte[] verKey;  // Verification (public key), point in GroupG2
    }
}

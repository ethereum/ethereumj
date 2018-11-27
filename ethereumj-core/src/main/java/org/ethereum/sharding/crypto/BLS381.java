package org.ethereum.sharding.crypto;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ECP2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ethereum.sharding.crypto.BLS381Milagro.ECP2_POINT_SIZE;
import static org.ethereum.sharding.crypto.BLS381Milagro.ECP_POINT_SIZE;
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
        return ecpToBytes(milagro.signMessage(BIG.fromBytes(sigKey), hash));
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

    public byte[] combineSignatures(List<byte[]> signatures) {
        ECP[] sigs = signatures.stream().map(ECP::fromBytes).toArray(ECP[]::new);
        ECP combinedSig = milagro.combine(sigs);

        return ecpToBytes(combinedSig);
    }

    public byte[] combineVerificationKeys(List<byte[]> verificationKeys) {
        ECP2[] verKeys = verificationKeys.stream().map(ECP2::fromBytes).toArray(ECP2[]::new);
        ECP2 combinedVerKey = milagro.combine(verKeys);

        return ecp2ToBytes(combinedVerKey);
    }

    private byte[] ecpToBytes(ECP point) {
        byte[] res = new byte[ECP_POINT_SIZE];
        point.toBytes(res, false);

        return res;
    }

    class KeyPair {
        byte[] sigKey;  // Signature (private key), point in GroupG1
        byte[] verKey;  // Verification (public key), point in GroupG2
    }
}

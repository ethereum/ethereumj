package org.ethereum.crypto;

import static java.util.Arrays.copyOfRange;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ethereum.util.ByteUtil;
import org.ethereum.util.Utils;
import org.spongycastle.util.encoders.Hex;

public class HashUtil {

    public static byte[] EMPTY_DATA_HASH = HashUtil.sha3(new byte[0]);

    private static final MessageDigest sha256digest;
    
    static {
        try {
            sha256digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }
    
    public static byte[] sha256(byte[] input) {
    	return sha256digest.digest(input);
    }

	public static byte[] sha3(byte[] input) {
		return SHA3Helper.sha3(input);
	}
	
	public static String sha3String(String input) {
		return SHA3Helper.sha3String(input);
	}
		
    /**
     * Calculates RIGTMOST160(SHA3(input)). This is used in address calculations.
     */
    public static byte[] sha3omit12(byte[] input) {
    	byte[] hash = sha3(input);
    	return copyOfRange(hash, 12, hash.length);
    }
    
    /**
     * See {@link ByteUtil#doubleDigest(byte[], int, int)}.
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in Bitcoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (sha256digest) {
            sha256digest.reset();
            sha256digest.update(input, offset, length);
            byte[] first = sha256digest.digest();
            return sha256digest.digest(first);
        }
    }


    /**
     * @return generates random peer id for the HelloMessage
     */
    public static byte[] randomPeerId(){

        byte[] peerIdBytes = new BigInteger(512, Utils.getRandom()).toByteArray();

        String peerId = null;
        if (peerIdBytes.length > 64)
            peerId = Hex.toHexString(peerIdBytes, 1, 64);
        else
            peerId = Hex.toHexString(peerIdBytes);

        return Hex.decode(peerId);
    }
}

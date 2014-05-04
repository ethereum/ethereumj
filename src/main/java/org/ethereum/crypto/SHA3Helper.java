package org.ethereum.crypto;

import java.math.BigInteger;

import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.util.encoders.Hex;

public class SHA3Helper {
	
	private static SHA3Digest DEFAULT_DIGEST_256 = new SHA3Digest(256);

	public static String sha3String(String message) {
		return sha3String(message, DEFAULT_DIGEST_256, true);
	}
	
	public static String sha3String(byte[] message) {
		return sha3String(message, DEFAULT_DIGEST_256, true);
	}

	public static byte[] sha3(String message) {
		return sha3(Hex.decode(message), DEFAULT_DIGEST_256, true);
	}
	
	public static byte[] sha3(byte[] message) {
		return sha3(message, DEFAULT_DIGEST_256, true);
	}
	
	protected static String sha3String(String message, Size bitSize) {
		SHA3Digest digest = new SHA3Digest(bitSize.bits);
		return sha3String(message, digest, true);
	}
	
	protected static String sha3String(byte[] message, Size bitSize) {
		SHA3Digest digest = new SHA3Digest(bitSize.bits);
		return sha3String(message, digest, true);
	}
	
	protected static String sha3String(String message, Size bitSize, boolean bouncyencoder) {
		SHA3Digest digest = new SHA3Digest(bitSize.bits);
		return sha3String(message, digest, bouncyencoder);
	}
	
	protected static String sha3string(byte[] message, Size bitSize, boolean bouncyencoder) {
		SHA3Digest digest = new SHA3Digest(bitSize.bits);
		return sha3String(message, digest, bouncyencoder);
	}
	
	private static String sha3String(String message, SHA3Digest digest, boolean bouncyencoder) {
		if (message != null) {
			return sha3String(Hex.decode(message), digest, bouncyencoder);
		}
		throw new NullPointerException("Can't hash a NULL value");
	}

	private static String sha3String(byte[] message, SHA3Digest digest, boolean bouncyencoder) {
		byte[] hash = doSha3(message, digest, bouncyencoder);
		if (bouncyencoder) {
			return Hex.toHexString(hash);
		} else {
			BigInteger bigInt = new BigInteger(1, hash);
			return bigInt.toString(16);
		}
	}
	
	private static byte[] sha3(byte[] message, SHA3Digest digest, boolean bouncyencoder) {
		return doSha3(message, digest, bouncyencoder);
	}
	
	private static byte[] doSha3(byte[] message, SHA3Digest digest, boolean bouncyencoder) {
		byte[] hash = new byte[digest.getDigestSize()];

		if (message.length != 0) {
			digest.update(message, 0, message.length);
		}
		digest.doFinal(hash, 0);
		return hash;
	}
	
    public enum Size {
        
        S224(224),
        S256(256),
        S384(384),
        S512(512);
        
        int bits = 0;
        
        Size(int bits) {
            this.bits = bits;
        }
        
        public int getValue() {
            return this.bits;
        }
    }
}

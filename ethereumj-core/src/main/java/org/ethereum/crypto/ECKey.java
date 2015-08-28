package org.ethereum.crypto;
/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;

import java.math.BigInteger;

import java.nio.charset.Charset;

import java.security.SecureRandom;
import java.security.SignatureException;

import java.util.Arrays;

import javax.annotation.Nullable;

import static org.ethereum.util.ByteUtil.bigIntegerToBytes;

/**
 * <p>Represents an elliptic curve public and (optionally) private key, usable for digital signatures but not encryption.
 * Creating a new ECKey with the empty constructor will generate a new random keypair. Other static methods can be used
 * when you already have the public or private parts. If you create a key with only the public part, you can check
 * signatures but not create them.</p>
 *
 * <p>The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a couple of discriminator bits can
 * be reversed to find the public key used to calculate it. This can be convenient when you have a message and a
 * signature and want to find out who signed it, rather than requiring the user to provide the expected identity.</p>
 *
 * <p>A key can be <i>compressed</i> or <i>uncompressed</i>. This refers to whether the public key is represented
 * when encoded into bytes as an (x, y) coordinate on the elliptic curve, or whether it's represented as just an X
 * co-ordinate and an extra byte that carries a sign bit. With the latter form the Y coordinate can be calculated
 * dynamically, however, <b>because the binary serialization is different the address of a key changes if its
 * compression status is changed</b>. If you deviate from the defaults it's important to understand this: money sent
 * to a compressed version of the key will have a different address to the same key in uncompressed form. Whether
 * a public key is compressed or not is recorded in the SEC binary serialisation format, and preserved in a flag in
 * this class so round-tripping preserves state. Unless you're working with old software or doing unusual things, you
 * can usually ignore the compressed/uncompressed distinction.</p>
 *
 * This code is borrowed from the bitcoinj project and altered to fit Ethereum.<br>
 * See <a href="https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/com/google/bitcoin/core/ECKey.java">
 * bitcoinj on GitHub</a>.
 */
public class ECKey implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ECKey.class);

    /**
     * The parameters of the secp256k1 curve that Ethereum uses.
     */
    public static final ECDomainParameters CURVE;

    /**
     * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't
     * sure what this is about, you can ignore it.
     */
    public static final BigInteger HALF_CURVE_ORDER;

    private static final SecureRandom secureRandom;
    private static final long serialVersionUID = -728224901792295832L;

    static {
        // All clients must agree on the curve to use by agreement. Ethereum uses secp256k1.
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        HALF_CURVE_ORDER = params.getN().shiftRight(1);
        secureRandom = new SecureRandom();
    }

    // The two parts of the key. If "priv" is set, "pub" can always be calculated. If "pub" is set but not "priv", we
    // can only verify signatures not make them.
    // TODO: Redesign this class to use consistent internals and more efficient serialization.
    private BigInteger priv;
    protected final ECPoint pub;

    // Transient because it's calculated on demand.
    transient private byte[] pubKeyHash;
    transient private byte[] nodeId;

    /**
     * Generates an entirely new keypair. Point compression is used so the resulting public key will be 33 bytes
     * (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    public ECKey() {
        this(secureRandom);
    }

    /**
     * Generates an entirely new keypair with the given {@link SecureRandom} object. Point compression is used so the
     * resulting public key will be 33 bytes (32 for the co-ordinate and 1 byte to represent the y bit).
     *
     * @param secureRandom -
     */
    public ECKey(SecureRandom secureRandom) {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        priv = privParams.getD();
        pub = CURVE.getCurve().decodePoint(pubParams.getQ().getEncoded(true));
    }

    public ECKey(@Nullable BigInteger priv, ECPoint pub) {
        this.priv = priv;
        if (pub == null)
            throw new IllegalArgumentException("Public key may not be null");
        this.pub = pub;
    }

    /**
     * Utility for compressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     *
     * @param uncompressed -
     *
     * @return -
     */
    public static ECPoint compressPoint(ECPoint uncompressed) {
        return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
    }

    /**
     * Utility for decompressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     *
     * @param compressed -
     *
     * @return  -
     */
    public static ECPoint decompressPoint(ECPoint compressed) {
        return CURVE.getCurve().decodePoint(compressed.getEncoded(false));
    }

    /**
     * Creates an ECKey given the private key only.  The public key is calculated from it (this is slow). Note that
     * the resulting public key is compressed.
     *
     * @param privKey -
     *
     *
     * @return  -
     */
    public static ECKey fromPrivate(BigInteger privKey) {
        return new ECKey(privKey, compressPoint(CURVE.getG().multiply(privKey)));
    }

    /**
     * Creates an ECKey given the private key only.  The public key is calculated from it (this is slow). The resulting
     * public key is compressed.
     *
     * @param privKeyBytes -
     *
     * @return -
     */
    public static ECKey fromPrivate(byte[] privKeyBytes) {
        return fromPrivate(new BigInteger(1, privKeyBytes));
    }

    /**
     * Creates an ECKey that simply trusts the caller to ensure that point is really the result of multiplying the
     * generator point by the private key. This is used to speed things up when you know you have the right values
     * already. The compression state of pub will be preserved.
     *
     * @param priv -
     * @param pub -
     *
     * @return  -
     */
    public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
        return new ECKey(priv, pub);
    }

    /**
     * Creates an ECKey that simply trusts the caller to ensure that point is really the result of multiplying the
     * generator point by the private key. This is used to speed things up when you know you have the right values
     * already. The compression state of the point will be preserved.
     *
     * @param priv -
     * @param pub -
     * @return -
     */
    public static ECKey fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
        check(priv != null, "Private key must not be null");
        check(pub != null, "Public key must not be null");
        return new ECKey(new BigInteger(1, priv), CURVE.getCurve().decodePoint(pub));
    }

    /**
     * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given point. The
     * compression state of pub will be preserved.
     *
     * @param pub -
     * @return -
     */
    public static ECKey fromPublicOnly(ECPoint pub) {
        return new ECKey(null, pub);
    }

    /**
     * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given encoded point.
     * The compression state of pub will be preserved.
     *
     * @param pub -
     * @return -
     */
    public static ECKey fromPublicOnly(byte[] pub) {
        return new ECKey(null, CURVE.getCurve().decodePoint(pub));
    }

    /**
     * Returns a copy of this key, but with the public point represented in uncompressed form. Normally you would
     * never need this: it's for specialised scenarios or when backwards compatibility in encoded form is necessary.
     *
     * @return  -
     */
    public ECKey decompress() {
        if (!pub.isCompressed())
            return this;
        else
            return new ECKey(priv, decompressPoint(pub));
    }

    /**
     * Returns true if this key doesn't have access to private key bytes. This may be because it was never
     * given any private key bytes to begin with (a watching key).
     *
     * @return -
     */
    public boolean isPubKeyOnly() {
        return priv == null;
    }

    /**
     * Returns true if this key has access to private key bytes. Does the opposite of
     * {@link #isPubKeyOnly()}.
     *
     * @return  -
     */
    public boolean hasPrivKey() {
        return priv != null;
    }

    /**
     * Returns public key bytes from the given private key. To convert a byte array into a BigInteger, use <tt>
     * new BigInteger(1, bytes);</tt>
     *
     * @param privKey -
     * @param compressed -
     * @return -
     */
    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = CURVE.getG().multiply(privKey);
        return point.getEncoded(compressed);
    }

    /**
     * Gets the hash160 form of the public key (as seen in addresses).
     *
     * @return -
     */
    public byte[] getAddress() {
        if (pubKeyHash == null) {
            byte[] pubBytes = this.pub.getEncoded(false);
            pubKeyHash = HashUtil.sha3omit12(Arrays.copyOfRange(pubBytes, 1, pubBytes.length));
        }
        return pubKeyHash;
    }

    /**
     * Generates the NodeID based on this key, that is the public key without first format byte
     */
    public byte[] getNodeId() {
        if (nodeId == null) {
            byte[] nodeIdWithFormat = getPubKey();
            nodeId = new byte[nodeIdWithFormat.length - 1];
            System.arraycopy(nodeIdWithFormat, 1, nodeId, 0, nodeId.length);
        }
        return nodeId;
    }

    /**
     * Gets the raw public key value. This appears in transaction scriptSigs. Note that this is <b>not</b> the same
     * as the pubKeyHash/address.
     *
     * @return  -
     */
    public byte[] getPubKey() {
        return pub.getEncoded();
    }

    /**
     * Gets the public key in the form of an elliptic curve point object from Bouncy Castle.
     *
     * @return  -
     */
    public ECPoint getPubKeyPoint() {
        return pub;
    }

    /**
     * Gets the private key in the form of an integer field element. The public key is derived by performing EC
     * point addition this number of times (i.e. point multiplying).
     *
     *
     * @return  -
     *
     * @throws java.lang.IllegalStateException if the private key bytes are not available.
     */
    public BigInteger getPrivKey() {
        if (priv == null)
            throw new MissingPrivateKeyException();
        return priv;
    }

    /**
     * Returns whether this key is using the compressed form or not. Compressed pubkeys are only 33 bytes, not 64.
     *
     *
     * @return  -
     */
    public boolean isCompressed() {
        return pub.isCompressed();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("pub:").append(Hex.toHexString(pub.getEncoded(false)));
        return b.toString();
    }

    /**
     * Produce a string rendering of the ECKey INCLUDING the private key.
     * Unless you absolutely need the private key it is better for security reasons to just use toString().
     *
     *
     * @return  -
     */
    public String toStringWithPrivate() {
        StringBuilder b = new StringBuilder();
        b.append(toString());
        if (priv != null) {
            b.append(" priv:").append(Hex.toHexString(priv.toByteArray()));
        }
        return b.toString();
    }

    /**
     * Groups the two components that make up a signature, and provides a way to encode to Base64 form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Ethereum protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    public static class ECDSASignature {
        /**
         * The two components of the signature.
         */
        public final BigInteger r, s;
        public byte v;

        /**
         * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
         *
         * @param r -
         * @param s -
         */
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         *
         * @param r
         * @param s
         * @return -
         */
        private static ECDSASignature fromComponents(byte[] r, byte[] s) {
            return new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        }

        /**
         *
         * @param r -
         * @param s -
         * @param v -
         * @return -
         */
        public static ECDSASignature fromComponents(byte[] r, byte[] s, byte v) {
            ECDSASignature signature = fromComponents(r, s);
            signature.v = v;
            return signature;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Ethereum transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         *
         * @return  -
         */
        public ECDSASignature toCanonicalised() {
            if (s.compareTo(HALF_CURVE_ORDER) > 0) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                return new ECDSASignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }

        /**
         *
         * @return -
         */
        public String toBase64() {
            byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
            sigData[0] = v;
            System.arraycopy(bigIntegerToBytes(this.r, 32), 0, sigData, 1, 32);
            System.arraycopy(bigIntegerToBytes(this.s, 32), 0, sigData, 33, 32);
            return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ECDSASignature signature = (ECDSASignature) o;

            if (!r.equals(signature.r)) return false;
            if (!s.equals(signature.s)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = r.hashCode();
            result = 31 * result + s.hashCode();
            return result;
        }
    }

    /**
     * Signs the given hash and returns the R and S components as BigIntegers
     * and put them in ECDSASignature
     *
     * @param input to sign
     * @return ECDSASignature signature that contains the R and S components
     */
    public ECDSASignature doSign(byte[] input) {
        // No decryption of private key required.
        if (priv == null)
            throw new MissingPrivateKeyException();
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(priv, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input);
        return new ECDSASignature(components[0], components[1]).toCanonicalised();
    }


    /**
     * Takes the sha3 hash (32 bytes) of data and returns the ECDSA signature
     *
     * @param messageHash -
     * @return -
     * @throws IllegalStateException if this ECKey does not have the private part.
     */
    public ECDSASignature sign(byte[] messageHash) {
        if (priv == null)
            throw new MissingPrivateKeyException();
        ECDSASignature sig = doSign(messageHash);
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            ECKey k = ECKey.recoverFromSignature(i, sig, messageHash, isCompressed());
            if (k != null && k.pub.equals(pub)) {
                recId = i;
                break;
            }
        }
        if (recId == -1)
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        sig.v = (byte) (recId + 27 + (isCompressed() ? 4 : 0));
        return sig;
    }


    /**
     * Given a piece of text and a message signature encoded in base64, returns an ECKey
     * containing the public key that was used to sign it. This can then be compared to the expected public key to
     * determine if the signature was correct.
     *
     * @param messageHash a piece of human readable text that was signed
     * @param signatureBase64 The Ethereum-format message signature in base64
     *
     * @return -
     * @throws SignatureException If the public key could not be recovered or if there was a signature format error.
     */
    public static ECKey signatureToKey(byte[] messageHash, String signatureBase64) throws SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException e) {
            // This is what you get back from Bouncy Castle if base64 doesn't decode :(
            throw new SignatureException("Could not decode base64", e);
        }
        // Parse the signature bytes into r/s and the selector value.
        if (signatureEncoded.length < 65)
            throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
        int header = signatureEncoded[0] & 0xFF;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34)
            throw new SignatureException("Header byte out of range: " + header);
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
        ECDSASignature sig = new ECDSASignature(r, s);
        boolean compressed = false;
        if (header >= 31) {
            compressed = true;
            header -= 4;
        }
        int recId = header - 27;
        ECKey key = ECKey.recoverFromSignature(recId, sig, messageHash, compressed);
        if (key == null)
            throw new SignatureException("Could not recover public key from signature");
        return key;
    }


    /**
     * Decrypt cipher by AES in SIC(also know as CTR) mode
     *
     * @param cipher -proper cipher
     * @return decrypted cipher, equal length to the cipher.
     */
    public byte[] decryptAES(byte[] cipher){

        if (priv == null)
            throw new MissingPrivateKeyException();


        AESFastEngine engine = new AESFastEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);

        KeyParameter key = new KeyParameter(BigIntegers.asUnsignedByteArray(priv));
        ParametersWithIV params = new ParametersWithIV(key, new byte[16]);

        ctrEngine.init(false, params);

        int i = 0;
        byte[] out = new byte[cipher.length];
        while(i < cipher.length){
            ctrEngine.processBlock(cipher, i, out, i);
            i += engine.getBlockSize();
            if (cipher.length - i  < engine.getBlockSize())
                break;
        }

        // process left bytes
        if (cipher.length - i > 0){
            byte[] tmpBlock = new byte[16];
            System.arraycopy(cipher, i, tmpBlock, 0, cipher.length - i);
            ctrEngine.processBlock(tmpBlock, 0, tmpBlock, 0);
            System.arraycopy(tmpBlock, 0, out, i, cipher.length - i);
        }

        return out;
    }



    /**
     * <p>Verifies the given ECDSA signature against the message bytes using the public key bytes.</p>
     *
     * <p>When using native ECDSA verification, data must be 32 bytes, and no element may be
     * larger than 520 bytes.</p>
     *
     * @param data Hash of the data to verify.
     * @param signature signature.
     * @param pub The public key bytes to use.
     *
     * @return -
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException npe) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted signatures.
            // Those signatures are inherently invalid/attack sigs so we just fail them here rather than crash the thread.
            logger.error("Caught NPE inside bouncy castle", npe);
            return false;
        }
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data Hash of the data to verify.
     * @param signature signature.
     * @param pub The public key bytes to use.
     *
     * @return  -
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        return verify(data, signature, pub);
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data Hash of the data to verify.
     * @param signature signature.
     *
     * @return -
     */
    public boolean verify(byte[] data, byte[] signature) {
        return ECKey.verify(data, signature, getPubKey());
    }


    /**
     * Verifies the given R/S pair (signature) against a hash using the public key.
     *
     * @param sigHash -
     * @param signature -
     * @return -
     */
    public boolean verify(byte[] sigHash, ECDSASignature signature) {
        return ECKey.verify(sigHash, signature, getPubKey());
    }

    /**
     * Returns true if this pubkey is canonical, i.e. the correct length taking into account compression.
     *
     * @return -
     */
    public boolean isPubKeyCanonical() {
        return isPubKeyCanonical(pub.getEncoded());
    }


    /**
     * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
     * @param pubkey -
     * @return -
     */
    public static boolean isPubKeyCanonical(byte[] pubkey) {
        if (pubkey[0] == 0x04) {
            // Uncompressed pubkey
            if (pubkey.length != 65)
                return false;
        } else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
            // Compressed pubkey
            if (pubkey.length != 33)
                return false;
        } else
            return false;
        return true;
    }

    /**
     * <p>Given the components of a signature and a selector value, recover and return the public key
     * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     *
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because
     * the key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
     * expecting.</p>
     *
     * <p>If this method returns null it means recovery was not possible and recId should be iterated.</p>
     *
     * <p>Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the
     * output is null OR a key that is not the one you expect, you try again with the next recId.</p>
     *
     * @param recId Which possible key to recover.
     * @param sig the R and S components of the signature, wrapped.
     * @param messageHash Hash of the data that was signed.
     * @param compressed Whether or not the original pubkey was compressed.
     * @return An ECKey containing only the public part, or null if recovery wasn't possible.
     */
    @Nullable
    public static ECKey recoverFromSignature(int recId, ECDSASignature sig, byte[] messageHash, boolean compressed) {
        check(recId >= 0, "recId must be positive");
        check(sig.r.signum() >= 0, "r must be positive");
        check(sig.s.signum() >= 0, "s must be positive");
        check(messageHash != null, "messageHash must not be null");
        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
        //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
        //        do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
        BigInteger prime = curve.getQ();  // Bouncy Castle is not consistent about the letter it uses for the prime.
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
        // So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
        if (!R.multiply(n).isInfinity())
            return null;
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = new BigInteger(1, messageHash);
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
        // ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
        return ECKey.fromPublicOnly(q.getEncoded(compressed));
    }


    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     *
     * @param xBN -
     * @param yBit -
     * @return -
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Returns a 32 byte array containing the private key, or null if the key is encrypted or public only
     *
     *  @return  -
     */
    @Nullable
    public byte[] getPrivKeyBytes() {
        return bigIntegerToBytes(priv, 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ECKey)) return false;

        ECKey ecKey = (ECKey) o;

        if (priv != null && !priv.equals(ecKey.priv)) return false;
        if (pub != null && !pub.equals(ecKey.pub)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        // Public keys are random already so we can just use a part of them as the hashcode. Read from the start to
        // avoid picking up the type code (compressed vs uncompressed) which is tacked on the end.
        byte[] bits = getPubKey();
        return (bits[0] & 0xFF) | ((bits[1] & 0xFF) << 8) | ((bits[2] & 0xFF) << 16) | ((bits[3] & 0xFF) << 24);
    }



    @SuppressWarnings("serial")
    public static class MissingPrivateKeyException extends RuntimeException {
    }

    private static void check(boolean test, String message) {
        if (!test) throw new IllegalArgumentException(message);
    }

}

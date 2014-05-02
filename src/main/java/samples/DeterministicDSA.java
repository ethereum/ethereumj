package samples;
// ==================================================================

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Deterministic DSA signature generation.  This is a sample
 * implementation designed to illustrate how deterministic DSA
 * chooses the pseudorandom value k when signing a given message.
 * This implementation was NOT optimized or hardened against
 * side-channel leaks.
 *
 * An instance is created with a hash function name, which must be
 * supported by the underlying Java virtual machine ("SHA-1" and
 * "SHA-256" should work everywhere).  The data to sign is input
 * through the {@code update()} methods.  The private key is set with
 * {@link #setPrivateKey}.  The signature is obtained by calling
 * {@link #sign}; alternatively, {@link #signHash} can be used to
 * sign some data that has been externally hashed.  The private key
 * MUST be set before generating the signature itself, but message
 * data can be input before setting the key.
 *
 * Instances are NOT thread-safe.  However, once a signature has
 * been generated, the same instance can be used again for another
 * signature; {@link #setPrivateKey} need not be called again if the
 * private key has not changed.  {@link #reset} can also be called to
 * cancel previously input data.  Generating a signature with {@link
 * #sign} (not {@link #signHash}) also implicitly causes a
 * reset.
 *
 * ------------------------------------------------------------------
 * Copyright (c) 2013 IETF Trust and the persons identified as
 * authors of the code.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted pursuant to, and subject to the license
 * terms contained in, the Simplified BSD License set forth in Section
 * 4.c of the IETF Trust's Legal Provisions Relating to IETF Documents
 * (http://trustee.ietf.org/license-info).
 *
 * Technical remarks and questions can be addressed to:
 * pornin@bolet.org
 * ------------------------------------------------------------------
 */

public class DeterministicDSA  {

    private String macName;
    private MessageDigest dig;
    private Mac hmac;
    private BigInteger p, q, g, x;
    private int qlen, rlen, rolen, holen;
    private byte[] bx;

    /**
     * Create an instance, using the specified hash function.
     * The name is used to obtain from the JVM an implementation
     * of the hash function and an implementation of HMAC.
     *
     * @param hashName   the hash function name
     * @throws IllegalArgumentException  on unsupported name
     */
    public DeterministicDSA(String hashName)
    {
        try {
            dig = MessageDigest.getInstance(hashName);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(nsae);
        }
        if (hashName.indexOf('-') < 0) {
            macName = "Hmac" + hashName;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Hmac");
            int n = hashName.length();
            for (int i = 0; i < n; i ++) {
                char c = hashName.charAt(i);
                if (c != '-') {
                    sb.append(c);
                }
            }
            macName = sb.toString();

        }
        try {
            hmac = Mac.getInstance(macName);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(nsae);
        }
        holen = hmac.getMacLength();
    }

    /**
     * Set the private key.
     *
     * @param p   key parameter: field modulus
     * @param q   key parameter: subgroup order
     * @param g   key parameter: generator
     * @param x   private key
     */
    public void setPrivateKey(BigInteger p, BigInteger q,
                              BigInteger g, BigInteger x)
    {
                  /*
                   * Perform some basic sanity checks.  We do not
                   * check primality of p or q because that would
                   * be too expensive.
                   *
                   * We reject keys where q is longer than 999 bits,
                   * because it would complicate signature encoding.
                   * Normal DSA keys do not have a q longer than 256
                   * bits anyway.
                   */
        if (p == null || q == null || g == null || x == null
                || p.signum() <= 0 || q.signum() <= 0
                || g.signum() <= 0 || x.signum() <= 0
                || x.compareTo(q) >= 0 || q.compareTo(p) >= 0
                || q.bitLength() > 999
                || g.compareTo(p) >= 0 || g.bitLength() == 1
                || g.modPow(q, p).bitLength() != 1) {
            throw new IllegalArgumentException(
                    "invalid DSA private key");
        }
        this.p = p;
        this.q = q;
        this.g = g;
        this.x = x;
        qlen = q.bitLength();
        if (q.signum() <= 0 || qlen < 8) {
            throw new IllegalArgumentException(
                    "bad group order: " + q);


        }
        rolen = (qlen + 7) >>> 3;
        rlen = rolen * 8;

                  /*
                   * Convert the private exponent (x) into a sequence
                   * of octets.
                   */
        bx = int2octets(x);
    }

    private BigInteger bits2int(byte[] in)
    {
        BigInteger v = new BigInteger(1, in);
        int vlen = in.length * 8;
        if (vlen > qlen) {
            v = v.shiftRight(vlen - qlen);
        }
        return v;
    }

    private byte[] int2octets(BigInteger v)
    {
        byte[] out = v.toByteArray();
        if (out.length < rolen) {
            byte[] out2 = new byte[rolen];
            System.arraycopy(out, 0,
                    out2, rolen - out.length,
                    out.length);
            return out2;
        } else if (out.length > rolen) {
            byte[] out2 = new byte[rolen];
            System.arraycopy(out, out.length - rolen,
                    out2, 0, rolen);
            return out2;
        } else {
            return out;
        }
    }

    private byte[] bits2octets(byte[] in)
    {
        BigInteger z1 = bits2int(in);
        BigInteger z2 = z1.subtract(q);
        return int2octets(z2.signum() < 0 ? z1 : z2);
    }

    /**



     Pornin                        Informational                    [Page 73]

     RFC 6979               Deterministic DSA and ECDSA           August 2013


     * Set (or reset) the secret key used for HMAC.
     *
     * @param K   the new secret key
     */
    private void setHmacKey(byte[] K)
    {
        try {
            hmac.init(new SecretKeySpec(K, macName));
        } catch (InvalidKeyException ike) {
            throw new IllegalArgumentException(ike);
        }
    }

    /**
     * Compute the pseudorandom k for signature generation,
     * using the process specified for deterministic DSA.
     *
     * @param h1   the hashed message
     * @return  the pseudorandom k to use
     */
    private BigInteger computek(byte[] h1)
    {
                  /*
                   * Convert hash value into an appropriately truncated
                   * and/or expanded sequence of octets.  The private
                   * key was already processed (into field bx[]).
                   */
        byte[] bh = bits2octets(h1);

                  /*
                   * HMAC is always used with K as key.
                   * Whenever K is updated, we reset the
                   * current HMAC key.
                   */

                  /* step b. */
        byte[] V = new byte[holen];
        for (int i = 0; i < holen; i ++) {
            V[i] = 0x01;
        }

                  /* step c. */
        byte[] K = new byte[holen];
        setHmacKey(K);

                  /* step d. */
        hmac.update(V);
        hmac.update((byte)0x00);
        hmac.update(bx);
        hmac.update(bh);
        K = hmac.doFinal();
        setHmacKey(K);

                  /* step e. */
        hmac.update(V);
        V = hmac.doFinal();

                  /* step f. */
        hmac.update(V);
        hmac.update((byte)0x01);
        hmac.update(bx);
        hmac.update(bh);
        K = hmac.doFinal();
        setHmacKey(K);

                  /* step g. */
        hmac.update(V);
        V = hmac.doFinal();

                  /* step h. */
        byte[] T = new byte[rolen];
        for (;;) {
                          /*
                           * We want qlen bits, but we support only
                           * hash functions with an output length
                           * multiple of 8;acd hence, we will gather
                           * rlen bits, i.e., rolen octets.
                           */
            int toff = 0;
            while (toff < rolen) {
                hmac.update(V);
                V = hmac.doFinal();
                int cc = Math.min(V.length,
                        T.length - toff);
                System.arraycopy(V, 0, T, toff, cc);
                toff += cc;
            }
            BigInteger k = bits2int(T);
            if (k.signum() > 0 && k.compareTo(q) < 0) {
                return k;
            }

                          /*
                           * k is not in the proper range; update
                           * K and V, and loop.
                           */

            hmac.update(V);
            hmac.update((byte)0x00);
            K = hmac.doFinal();
            setHmacKey(K);
            hmac.update(V);
            V = hmac.doFinal();
        }
    }

    /**
     * Process one more byte of input data (message to sign).
     *
     * @param in   the extra input byte
     */
    public void update(byte in)
    {
        dig.update(in);
    }

    /**
     * Process some extra bytes of input data (message to sign).
     *
     * @param in   the extra input bytes
     */
    public void update(byte[] in)
    {
        dig.update(in, 0, in.length);
    }

    /**
     * Process some extra bytes of input data (message to sign).
     *
     * @param in    the extra input buffer
     * @param off   the extra input offset
     * @param len   the extra input length (in bytes)
     */
    public void update(byte[] in, int off, int len)
    {
        dig.update(in, off, len);
    }

    /**
     * Produce the signature.  {@link #setPrivateKey} MUST have
     * been called.  The signature is computed over the data
     * that was input through the {@code update*()} methods.
     * This engine is then reset (made ready for a new
     * signature generation).
     *



     Pornin                        Informational                    [Page 76]

     RFC 6979               Deterministic DSA and ECDSA           August 2013


     * @return  the signature
     */
    public byte[] sign()
    {
        return signHash(dig.digest());
    }

    /**
     * Produce the signature.  {@link #setPrivateKey} MUST
     * have been called.  The signature is computed over the
     * provided hash value (data is assumed to have been hashed
     * externally).  The data that was input through the
     * {@code update*()} methods is ignored, but kept.
     *
     * If the hash output is longer than the subgroup order
     * (the length of q, in bits, denoted 'qlen'), then the
     * provided value {@code h1} can be truncated, provided that
     * at least qlen leading bits are preserved.  In other words,
     * bit values in {@code h1} beyond the first qlen bits are
     * ignored.
     *
     * @param h1   the hash value
     * @return  the signature
     */
    public byte[] signHash(byte[] h1)
    {
        if (p == null) {
            throw new IllegalStateException(
                    "no private key set");
        }
        try {
            BigInteger k = computek(h1);
            BigInteger r = g.modPow(k, p).mod(q);
            BigInteger s = k.modInverse(q).multiply(
                    bits2int(h1).add(x.multiply(r)))
                    .mod(q);

                          /*
                           * Signature encoding: ASN.1 SEQUENCE of
                           * two INTEGERs.  The conditions on q
                           * imply that the encoded version of r and
                           * s is no longer than 127 bytes for each,
                           * including DER tag and length.
                           */
            byte[] br = r.toByteArray();
            byte[] bs = s.toByteArray();
            int ulen = br.length + bs.length + 4;
            int slen = ulen + (ulen >= 128 ? 3 : 2);

            byte[] sig = new byte[slen];
            int i = 0;
            sig[i ++] = 0x30;
            if (ulen >= 128) {
                sig[i ++] = (byte)0x81;
                sig[i ++] = (byte)ulen;
            } else {
                sig[i ++] = (byte)ulen;
            }
            sig[i ++] = 0x02;
            sig[i ++] = (byte)br.length;
            System.arraycopy(br, 0, sig, i, br.length);
            i += br.length;
            sig[i ++] = 0x02;
            sig[i ++] = (byte)bs.length;
            System.arraycopy(bs, 0, sig, i, bs.length);
            return sig;

        } catch (ArithmeticException ae) {
            throw new IllegalArgumentException(
                    "DSA error (bad key ?)", ae);
        }
    }

    /**
     * Reset this engine.  Data input through the {@code
     * update*()} methods is discarded.  The current private key,
     * if one was set, is kept unchanged.
     */
    public void reset()
    {
        dig.reset();
    }
}

// ==================================================================








/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.spongycastle.crypto.*;
import org.spongycastle.crypto.generators.EphemeralKeyPairGenerator;
import org.spongycastle.crypto.params.*;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.Pack;

/**
 * Support class for constructing integrated encryption cipher
 * for doing basic message exchanges on top of key agreement ciphers.
 * Follows the description given in IEEE Std 1363a with a couple of changes
 * specific to Ethereum:
 * - Hash the MAC key before use
 * - Include the encryption IV in the MAC computation
 */
public class EthereumIESEngine
{
    private final Digest hash;
    BasicAgreement agree;
    DerivationFunction kdf;
    Mac mac;
    BufferedBlockCipher cipher;
    byte[] macBuf;

    boolean forEncryption;
    CipherParameters privParam, pubParam;
    IESParameters param;

    byte[] V;
    private EphemeralKeyPairGenerator keyPairGenerator;
    private KeyParser keyParser;
    private byte[] IV;
    boolean hashK2 = true;

    /**
     * set up for use with stream mode, where the key derivation function
     * is used to provide a stream of bytes to xor with the message.
     *  @param agree the key agreement used as the basis for the encryption
     * @param kdf    the key derivation function used for byte generation
     * @param mac    the message authentication code generator for the message
     * @param hash   hash ing function
     * @param cipher the actual cipher
     */
    public EthereumIESEngine(
            BasicAgreement agree,
            DerivationFunction kdf,
            Mac mac, Digest hash, BufferedBlockCipher cipher)
    {
        this.agree = agree;
        this.kdf = kdf;
        this.mac = mac;
        this.hash = hash;
        this.macBuf = new byte[mac.getMacSize()];
        this.cipher = cipher;
    }


    public void setHashMacKey(boolean hashK2) {
        this.hashK2 = hashK2;
    }

    /**
     * Initialise the encryptor.
     *
     * @param forEncryption whether or not this is encryption/decryption.
     * @param privParam     our private key parameters
     * @param pubParam      the recipient's/sender's public key parameters
     * @param params        encoding and derivation parameters, may be wrapped to include an IV for an underlying block cipher.
     */
    public void init(
        boolean forEncryption,
        CipherParameters privParam,
        CipherParameters pubParam,
        CipherParameters params)
    {
        this.forEncryption = forEncryption;
        this.privParam = privParam;
        this.pubParam = pubParam;
        this.V = new byte[0];

        extractParams(params);
    }


    /**
     * Initialise the encryptor.
     *
     * @param publicKey      the recipient's/sender's public key parameters
     * @param params         encoding and derivation parameters, may be wrapped to include an IV for an underlying block cipher.
     * @param ephemeralKeyPairGenerator             the ephemeral key pair generator to use.
     */
    public void init(AsymmetricKeyParameter publicKey, CipherParameters params, EphemeralKeyPairGenerator ephemeralKeyPairGenerator)
    {
        this.forEncryption = true;
        this.pubParam = publicKey;
        this.keyPairGenerator = ephemeralKeyPairGenerator;

        extractParams(params);
    }

    /**
     * Initialise the encryptor.
     *
     * @param privateKey      the recipient's private key.
     * @param params          encoding and derivation parameters, may be wrapped to include an IV for an underlying block cipher.
     * @param publicKeyParser the parser for reading the ephemeral public key.
     */
    public void init(AsymmetricKeyParameter privateKey, CipherParameters params, KeyParser publicKeyParser)
    {
        this.forEncryption = false;
        this.privParam = privateKey;
        this.keyParser = publicKeyParser;

        extractParams(params);
    }

    private void extractParams(CipherParameters params)
    {
        if (params instanceof ParametersWithIV)
        {
            this.IV = ((ParametersWithIV)params).getIV();
            this.param = (IESParameters)((ParametersWithIV)params).getParameters();
        }
        else
        {
            this.IV = null;
            this.param = (IESParameters)params;
        }
    }

    public BufferedBlockCipher getCipher()
    {
        return cipher;
    }

    public Mac getMac()
    {
        return mac;
    }

    private byte[] encryptBlock(
        byte[] in,
        int inOff,
        int inLen,
        byte[] macData)
        throws InvalidCipherTextException
    {
        byte[] C = null, K = null, K1 = null, K2 = null;
        int len;

        if (cipher == null)
        {
            // Streaming mode.
            K1 = new byte[inLen];
            K2 = new byte[param.getMacKeySize() / 8];
            K = new byte[K1.length + K2.length];

            kdf.generateBytes(K, 0, K.length);

//            if (V.length != 0)
//            {
//                System.arraycopy(K, 0, K2, 0, K2.length);
//                System.arraycopy(K, K2.length, K1, 0, K1.length);
//            }
//            else
            {
                System.arraycopy(K, 0, K1, 0, K1.length);
                System.arraycopy(K, inLen, K2, 0, K2.length);
            }

            C = new byte[inLen];

            for (int i = 0; i != inLen; i++)
            {
                C[i] = (byte)(in[inOff + i] ^ K1[i]);
            }
            len = inLen;
        }
        else
        {
            // Block cipher mode.
            K1 = new byte[((IESWithCipherParameters)param).getCipherKeySize() / 8];
            K2 = new byte[param.getMacKeySize() / 8];
            K = new byte[K1.length + K2.length];

            kdf.generateBytes(K, 0, K.length);
            System.arraycopy(K, 0, K1, 0, K1.length);
            System.arraycopy(K, K1.length, K2, 0, K2.length);

            // If iv provided use it to initialise the cipher
            if (IV != null)
            {
                cipher.init(true, new ParametersWithIV(new KeyParameter(K1), IV));
            }
            else
            {
                cipher.init(true, new KeyParameter(K1));
            }

            C = new byte[cipher.getOutputSize(inLen)];
            len = cipher.processBytes(in, inOff, inLen, C, 0);
            len += cipher.doFinal(C, len);
        }


        // Convert the length of the encoding vector into a byte array.
        byte[] P2 = param.getEncodingV();

        // Apply the MAC.
        byte[] T = new byte[mac.getMacSize()];

        byte[] K2a;
        if (hashK2) {
            K2a = new byte[hash.getDigestSize()];
            hash.reset();
            hash.update(K2, 0, K2.length);
            hash.doFinal(K2a, 0);
        } else {
            K2a = K2;
        }
        mac.init(new KeyParameter(K2a));
        mac.update(IV, 0, IV.length);
        mac.update(C, 0, C.length);
        if (P2 != null)
        {
            mac.update(P2, 0, P2.length);
        }
        if (V.length != 0 && P2 != null) {
            byte[] L2 = new byte[4];
            Pack.intToBigEndian(P2.length * 8, L2, 0);
            mac.update(L2, 0, L2.length);
        }

        if (macData != null) {
            mac.update(macData, 0, macData.length);
        }

        mac.doFinal(T, 0);

        // Output the triple (V,C,T).
        byte[] Output = new byte[V.length + len + T.length];
        System.arraycopy(V, 0, Output, 0, V.length);
        System.arraycopy(C, 0, Output, V.length, len);
        System.arraycopy(T, 0, Output, V.length + len, T.length);
        return Output;
    }

    private byte[] decryptBlock(
        byte[] in_enc,
        int inOff,
        int inLen,
        byte[] macData)
        throws InvalidCipherTextException
    {
        byte[] M = null, K = null, K1 = null, K2 = null;
        int len;

        // Ensure that the length of the input is greater than the MAC in bytes
        if (inLen <= (param.getMacKeySize() / 8))
        {
            throw new InvalidCipherTextException("Length of input must be greater than the MAC");
        }

        if (cipher == null)
        {
            // Streaming mode.
            K1 = new byte[inLen - V.length - mac.getMacSize()];
            K2 = new byte[param.getMacKeySize() / 8];
            K = new byte[K1.length + K2.length];

            kdf.generateBytes(K, 0, K.length);

//            if (V.length != 0)
//            {
//                System.arraycopy(K, 0, K2, 0, K2.length);
//                System.arraycopy(K, K2.length, K1, 0, K1.length);
//            }
//            else
            {
                System.arraycopy(K, 0, K1, 0, K1.length);
                System.arraycopy(K, K1.length, K2, 0, K2.length);
            }

            M = new byte[K1.length];

            for (int i = 0; i != K1.length; i++)
            {
                M[i] = (byte)(in_enc[inOff + V.length + i] ^ K1[i]);
            }

            len = K1.length;
        }
        else
        {
            // Block cipher mode.
            K1 = new byte[((IESWithCipherParameters)param).getCipherKeySize() / 8];
            K2 = new byte[param.getMacKeySize() / 8];
            K = new byte[K1.length + K2.length];

            kdf.generateBytes(K, 0, K.length);
            System.arraycopy(K, 0, K1, 0, K1.length);
            System.arraycopy(K, K1.length, K2, 0, K2.length);

            // If IV provide use it to initialize the cipher
            if (IV != null)
            {
                cipher.init(false, new ParametersWithIV(new KeyParameter(K1), IV));
            }
            else
            {
                cipher.init(false, new KeyParameter(K1));
            }

            M = new byte[cipher.getOutputSize(inLen - V.length - mac.getMacSize())];
            len = cipher.processBytes(in_enc, inOff + V.length, inLen - V.length - mac.getMacSize(), M, 0);
            len += cipher.doFinal(M, len);
        }


        // Convert the length of the encoding vector into a byte array.
        byte[] P2 = param.getEncodingV();

        // Verify the MAC.
        int end = inOff + inLen;
        byte[] T1 = Arrays.copyOfRange(in_enc, end - mac.getMacSize(), end);

        byte[] T2 = new byte[T1.length];
        byte[] K2a;
        if (hashK2) {
            K2a = new byte[hash.getDigestSize()];
            hash.reset();
            hash.update(K2, 0, K2.length);
            hash.doFinal(K2a, 0);
        } else {
            K2a = K2;
        }
        mac.init(new KeyParameter(K2a));
        mac.update(IV, 0, IV.length);
        mac.update(in_enc, inOff + V.length, inLen - V.length - T2.length);

        if (P2 != null)
        {
            mac.update(P2, 0, P2.length);
        }

        if (V.length != 0 && P2 != null) {
            byte[] L2 = new byte[4];
            Pack.intToBigEndian(P2.length * 8, L2, 0);
            mac.update(L2, 0, L2.length);
        }

        if (macData != null) {
            mac.update(macData, 0, macData.length);
        }

        mac.doFinal(T2, 0);

        if (!Arrays.constantTimeAreEqual(T1, T2))
        {
            throw new InvalidCipherTextException("Invalid MAC.");
        }


        // Output the message.
        return Arrays.copyOfRange(M, 0, len);
    }

    public byte[] processBlock(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        return processBlock(in, inOff, inLen, null);
    }

    public byte[] processBlock(
        byte[] in,
        int inOff,
        int inLen,
        byte[] macData)
        throws InvalidCipherTextException
    {
        if (forEncryption)
        {
            if (keyPairGenerator != null)
            {
                EphemeralKeyPair ephKeyPair = keyPairGenerator.generate();

                this.privParam = ephKeyPair.getKeyPair().getPrivate();
                this.V = ephKeyPair.getEncodedPublicKey();
            }
        }
        else
        {
            if (keyParser != null)
            {
                ByteArrayInputStream bIn = new ByteArrayInputStream(in, inOff, inLen);

                try
                {
                    this.pubParam = keyParser.readKey(bIn);
                }
                catch (IOException e)
                {
                    throw new InvalidCipherTextException("unable to recover ephemeral public key: " + e.getMessage(), e);
                }

                int encLength = (inLen - bIn.available());
                this.V = Arrays.copyOfRange(in, inOff, inOff + encLength);
            }
        }

        // Compute the common value and convert to byte array.
        agree.init(privParam);
        BigInteger z = agree.calculateAgreement(pubParam);
        byte[] Z = BigIntegers.asUnsignedByteArray(agree.getFieldSize(), z);

        // Create input to KDF.
        byte[] VZ;
//        if (V.length != 0)
//        {
//            VZ = new byte[V.length + Z.length];
//            System.arraycopy(V, 0, VZ, 0, V.length);
//            System.arraycopy(Z, 0, VZ, V.length, Z.length);
//        }
//        else
        {
            VZ = Z;
        }

        // Initialise the KDF.
        DerivationParameters kdfParam;
        if (kdf instanceof MGF1BytesGeneratorExt) {
            kdfParam = new MGFParameters(VZ);
        } else {
            kdfParam = new KDFParameters(VZ, param.getDerivationV());
        }
        kdf.init(kdfParam);

        return forEncryption
            ? encryptBlock(in, inOff, inLen, macData)
            : decryptBlock(in, inOff, inLen, macData);
    }
}

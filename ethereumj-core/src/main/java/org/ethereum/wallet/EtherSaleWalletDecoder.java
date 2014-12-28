package org.ethereum.wallet;

import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.ExtendedDigest;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.util.Arrays;


public class EtherSaleWalletDecoder {

    public static final int HASH_ITERATIONS = 2000;
    public static final int DERIVED_KEY_BIT_COUNT = 128;
    public static final int IV_LENGTH = 16;

    private EtherSaleWallet etherSaleWallet;

    public EtherSaleWalletDecoder(final EtherSaleWallet wallet) {
        etherSaleWallet = wallet;
    }

    public byte[] getPrivateKey(final String password) throws InvalidCipherTextException {
        byte[] passwordHash = generatePasswordHash(password);
        byte[] decryptedSeed = decryptSeed(passwordHash, etherSaleWallet.getEncseedBytes());
        return hashSeed(decryptedSeed);
    }

    /* VisibleForTesting */
    protected byte[] generatePasswordHash(final String password) {
        char[] chars = password.toCharArray();
        byte[] salt = password.getBytes();

        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(chars), salt, HASH_ITERATIONS);
        return ((KeyParameter) generator.generateDerivedParameters(DERIVED_KEY_BIT_COUNT)).getKey();
    }

    private byte[] hashSeed(final byte[] seed) {
        ExtendedDigest md = new SHA3Digest(256);
        md.update(seed, 0, seed.length);
        byte[] result = new byte[md.getDigestSize()];
        md.doFinal(result, 0);
        return result;
    }

    protected byte[] decryptSeed(byte[] pbkdf2PasswordHash, byte[] encseedBytesWithIV) throws
            InvalidCipherTextException {

        // first 16 bytes are the IV (0-15)
        byte[] ivBytes = Arrays.copyOf(encseedBytesWithIV, IV_LENGTH);
        // use bytes 16 to the end for encrypted seed
        byte[] encData = Arrays.copyOfRange(encseedBytesWithIV, IV_LENGTH, encseedBytesWithIV.length);

        // setup cipher parameters with key and IV
        KeyParameter keyParam = new KeyParameter(pbkdf2PasswordHash);
        CipherParameters params = new ParametersWithIV(keyParam, ivBytes);

        // setup AES cipher in CBC mode with PKCS7 padding
        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
        cipher.reset();
        cipher.init(false, params);

        // create a temporary buffer to decode into (it'll include padding)
        byte[] buffer = new byte[cipher.getOutputSize(encData.length)];
        int length = cipher.processBytes(encData, 0, encData.length, buffer, 0);
        length += cipher.doFinal(buffer, length);

        // remove padding
        byte[] result = new byte[length];
        System.arraycopy(buffer, 0, result, 0, length);

        return result;
    }
}

package org.ethereum.crypto;

import org.ethereum.ConcatKDFBytesGenerator;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.generators.KDF2BytesGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import static org.ethereum.crypto.ECKey.CURVE;

public class ECIESCoder {


    public static final int MAC_KEY_SIZE = 128;


    public static byte[] decrypt(BigInteger privKey, byte[] cipher) {

        byte[] plaintext = new byte[0];
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(cipher);
            byte[] ephemBytes = new byte[2*((CURVE.getCurve().getFieldSize()+7)/8) + 1];
            is.read(ephemBytes);
            ECPoint ephem = CURVE.getCurve().decodePoint(ephemBytes);
            byte[] IV = new byte[MAC_KEY_SIZE/8];
            is.read(IV);
            byte[] cipherBody = new byte[is.available()];
            is.read(cipherBody);

            plaintext = decrypt(ephem, privKey, IV, cipherBody);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return plaintext;
    }

    public static byte[] decrypt(ECPoint ephem, BigInteger prv, byte[] IV, byte[] cipher) throws Throwable {
        AESFastEngine aesFastEngine = new AESFastEngine();

        EthereumIESEngine iesEngine = new EthereumIESEngine(
                new ECDHBasicAgreement(),
                new ConcatKDFBytesGenerator(new SHA256Digest()),
                new HMac(new SHA256Digest()),
                new SHA256Digest(),
                new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));


        byte[]         d = new byte[] {};
        byte[]         e = new byte[] {};

        IESParameters p = new IESWithCipherParameters(d, e, MAC_KEY_SIZE, MAC_KEY_SIZE);
        ParametersWithIV parametersWithIV =
                new ParametersWithIV(p, IV);

        iesEngine.init(false, new ECPrivateKeyParameters(prv, CURVE), new ECPublicKeyParameters(ephem, CURVE), parametersWithIV);

        byte[] message = iesEngine.processBlock(cipher, 0, cipher.length);
        return message;
    }


    public static byte[] encrypt(byte[] plaintext) {
        AESFastEngine aesFastEngine = new AESFastEngine();

        EthereumIESEngine iesEngine = new EthereumIESEngine(
                new ECDHBasicAgreement(),
                new KDF2BytesGenerator(new SHA256Digest()),
                new HMac(new SHA256Digest()),
                new SHA256Digest(),
                new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));


        byte[]         d = new byte[] {};
        byte[]         e = new byte[] {};

        IESParameters p = new IESWithCipherParameters(d, e, 256, MAC_KEY_SIZE);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, new byte[256/8]);

        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        SecureRandom random = new SecureRandom();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(CURVE, random);

        eGen.init(gParam);


        AsymmetricCipherKeyPair p1 = eGen.generateKeyPair();
        AsymmetricCipherKeyPair p2 = eGen.generateKeyPair();


        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, random);
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keygenParams);

        ECKeyPairGenerator gen = new ECKeyPairGenerator();
        gen.init(new ECKeyGenerationParameters(CURVE, random));

        iesEngine.init(true, p1.getPrivate(), p2.getPublic(), parametersWithIV);

        byte[] cipher = new byte[0];
        try {
            cipher = iesEngine.processBlock(plaintext, 0, plaintext.length);
        } catch (InvalidCipherTextException e1) {e1.printStackTrace();}
        return cipher;
    }

}

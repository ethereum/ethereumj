package org.ethereum.crypto;

import com.google.common.base.Throwables;
import org.ethereum.ConcatKDFBytesGenerator;
import org.spongycastle.crypto.*;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import static org.ethereum.crypto.ECKey.CURVE;

public class ECIESCoder {


    public static final int KEY_SIZE = 128;


    public static byte[] decrypt(BigInteger privKey, byte[] cipher) throws IOException, InvalidCipherTextException {

        byte[] plaintext;

        ByteArrayInputStream is = new ByteArrayInputStream(cipher);
        byte[] ephemBytes = new byte[2*((CURVE.getCurve().getFieldSize()+7)/8) + 1];

        is.read(ephemBytes);
        ECPoint ephem = CURVE.getCurve().decodePoint(ephemBytes);
        byte[] IV = new byte[KEY_SIZE /8];
        is.read(IV);
        byte[] cipherBody = new byte[is.available()];
        is.read(cipherBody);

        plaintext = decrypt(ephem, privKey, IV, cipherBody);

        return plaintext;
    }

    public static byte[] decrypt(ECPoint ephem, BigInteger prv, byte[] IV, byte[] cipher) throws InvalidCipherTextException {
        AESFastEngine aesFastEngine = new AESFastEngine();

        EthereumIESEngine iesEngine = new EthereumIESEngine(
                new ECDHBasicAgreement(),
                new ConcatKDFBytesGenerator(new SHA256Digest()),
                new HMac(new SHA256Digest()),
                new SHA256Digest(),
                new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));


        byte[]         d = new byte[] {};
        byte[]         e = new byte[] {};

        IESParameters p = new IESWithCipherParameters(d, e, KEY_SIZE, KEY_SIZE);
        ParametersWithIV parametersWithIV =
                new ParametersWithIV(p, IV);

        iesEngine.init(false, new ECPrivateKeyParameters(prv, CURVE), new ECPublicKeyParameters(ephem, CURVE), parametersWithIV);

        return iesEngine.processBlock(cipher, 0, cipher.length);
    }


    public static byte[] encrypt(ECPoint toPub, byte[] plaintext) {

        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        SecureRandom random = new SecureRandom();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(CURVE, random);

        eGen.init(gParam);

        byte[] IV = new byte[KEY_SIZE/8];
        new SecureRandom().nextBytes(IV);

        AsymmetricCipherKeyPair ephemPair = eGen.generateKeyPair();
        BigInteger prv = ((ECPrivateKeyParameters)ephemPair.getPrivate()).getD();
        ECPoint pub = ((ECPublicKeyParameters)ephemPair.getPublic()).getQ();
        EthereumIESEngine iesEngine = makeIESEngine(true, toPub, prv, IV);


        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, random);
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keygenParams);

        ECKeyPairGenerator gen = new ECKeyPairGenerator();
        gen.init(new ECKeyGenerationParameters(ECKey.CURVE, random));

        byte[] cipher;
        try {
            cipher = iesEngine.processBlock(plaintext, 0, plaintext.length);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(pub.getEncoded(false));
            bos.write(IV);
            bos.write(cipher);
            return bos.toByteArray();
        } catch (InvalidCipherTextException e) {
            throw Throwables.propagate(e);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }


    private static EthereumIESEngine makeIESEngine(boolean isEncrypt, ECPoint pub, BigInteger prv, byte[] IV) {
        AESFastEngine aesFastEngine = new AESFastEngine();

        EthereumIESEngine iesEngine = new EthereumIESEngine(
                new ECDHBasicAgreement(),
                new ConcatKDFBytesGenerator(new SHA256Digest()),
                new HMac(new SHA256Digest()),
                new SHA256Digest(),
                new BufferedBlockCipher(new SICBlockCipher(aesFastEngine)));


        byte[]         d = new byte[] {};
        byte[]         e = new byte[] {};

        IESParameters p = new IESWithCipherParameters(d, e, KEY_SIZE, KEY_SIZE);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, IV);

        iesEngine.init(isEncrypt, new ECPrivateKeyParameters(prv, CURVE), new ECPublicKeyParameters(pub, CURVE), parametersWithIV);
        return iesEngine;
    }

    public static int getOverhead() {
        // 256 bit EC public key, IV, 256 bit MAC
        return 65 + KEY_SIZE/8 + 32;
    }
}

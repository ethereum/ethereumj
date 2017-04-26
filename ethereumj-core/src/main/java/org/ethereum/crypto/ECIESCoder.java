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

import com.google.common.base.Throwables;
import org.ethereum.ConcatKDFBytesGenerator;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.generators.EphemeralKeyPairGenerator;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.parsers.ECIESPublicKeyParser;
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
        return decrypt(privKey, cipher, null);
    }

    public static byte[] decrypt(BigInteger privKey, byte[] cipher, byte[] macData) throws IOException, InvalidCipherTextException {

        byte[] plaintext;

        ByteArrayInputStream is = new ByteArrayInputStream(cipher);
        byte[] ephemBytes = new byte[2*((CURVE.getCurve().getFieldSize()+7)/8) + 1];

        is.read(ephemBytes);
        ECPoint ephem = CURVE.getCurve().decodePoint(ephemBytes);
        byte[] IV = new byte[KEY_SIZE /8];
        is.read(IV);
        byte[] cipherBody = new byte[is.available()];
        is.read(cipherBody);

        plaintext = decrypt(ephem, privKey, IV, cipherBody, macData);

        return plaintext;
    }

    public static byte[] decrypt(ECPoint ephem, BigInteger prv, byte[] IV, byte[] cipher, byte[] macData) throws InvalidCipherTextException {
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

        return iesEngine.processBlock(cipher, 0, cipher.length, macData);
    }

    /**
     *  Encryption equivalent to the Crypto++ default ECIES<ECP> settings:
     *
     *  DL_KeyAgreementAlgorithm:        DL_KeyAgreementAlgorithm_DH<struct ECPPoint,struct EnumToType<enum CofactorMultiplicationOption,0> >
     *  DL_KeyDerivationAlgorithm:       DL_KeyDerivationAlgorithm_P1363<struct ECPPoint,0,class P1363_KDF2<class SHA1> >
     *  DL_SymmetricEncryptionAlgorithm: DL_EncryptionAlgorithm_Xor<class HMAC<class SHA1>,0>
     *  DL_PrivateKey:                   DL_Key<ECPPoint>
     *  DL_PrivateKey_EC<class ECP>
     *
     *  Used for Whisper V3
     */
    public static byte[] decryptSimple(BigInteger privKey, byte[] cipher) throws IOException, InvalidCipherTextException {
        EthereumIESEngine iesEngine = new EthereumIESEngine(
                new ECDHBasicAgreement(),
                new MGF1BytesGeneratorExt(new SHA1Digest(), 1),
                new HMac(new SHA1Digest()),
                new SHA1Digest(),
                null);

        IESParameters p = new IESParameters(null, null, KEY_SIZE);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, new byte[0]);

        iesEngine.setHashMacKey(false);

        iesEngine.init(new ECPrivateKeyParameters(privKey, CURVE), parametersWithIV,
                new ECIESPublicKeyParser(ECKey.CURVE));

        return iesEngine.processBlock(cipher, 0, cipher.length);
    }

    public static byte[] encrypt(ECPoint toPub, byte[] plaintext) {
        return encrypt(toPub, plaintext, null);
    }

    public static byte[] encrypt(ECPoint toPub, byte[] plaintext, byte[] macData) {

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
            cipher = iesEngine.processBlock(plaintext, 0, plaintext.length, macData);
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

    /**
     *  Encryption equivalent to the Crypto++ default ECIES<ECP> settings:
     *
     *  DL_KeyAgreementAlgorithm:        DL_KeyAgreementAlgorithm_DH<struct ECPPoint,struct EnumToType<enum CofactorMultiplicationOption,0> >
     *  DL_KeyDerivationAlgorithm:       DL_KeyDerivationAlgorithm_P1363<struct ECPPoint,0,class P1363_KDF2<class SHA1> >
     *  DL_SymmetricEncryptionAlgorithm: DL_EncryptionAlgorithm_Xor<class HMAC<class SHA1>,0>
     *  DL_PrivateKey:                   DL_Key<ECPPoint>
     *  DL_PrivateKey_EC<class ECP>
     *
     *  Used for Whisper V3
     */
    public static byte[] encryptSimple(ECPoint pub, byte[] plaintext) throws IOException, InvalidCipherTextException {
        EthereumIESEngine iesEngine = new EthereumIESEngine(
                new ECDHBasicAgreement(),
                new MGF1BytesGeneratorExt(new SHA1Digest(), 1),
                new HMac(new SHA1Digest()),
                new SHA1Digest(),
                null);

        IESParameters p = new IESParameters(null, null, KEY_SIZE);
        ParametersWithIV parametersWithIV = new ParametersWithIV(p, new byte[0]);

        iesEngine.setHashMacKey(false);

        ECKeyPairGenerator eGen = new ECKeyPairGenerator();
        SecureRandom random = new SecureRandom();
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(CURVE, random);
        eGen.init(gParam);

//        AsymmetricCipherKeyPairGenerator testGen = new AsymmetricCipherKeyPairGenerator() {
//            ECKey priv = ECKey.fromPrivate(Hex.decode("d0b043b4c5d657670778242d82d68a29d25d7d711127d17b8e299f156dad361a"));
//
//            @Override
//            public void init(KeyGenerationParameters keyGenerationParameters) {
//            }
//
//            @Override
//            public AsymmetricCipherKeyPair generateKeyPair() {
//                return new AsymmetricCipherKeyPair(new ECPublicKeyParameters(priv.getPubKeyPoint(), CURVE),
//                        new ECPrivateKeyParameters(priv.getPrivKey(), CURVE));
//            }
//        };

        EphemeralKeyPairGenerator ephemeralKeyPairGenerator =
                new EphemeralKeyPairGenerator(/*testGen*/eGen, new ECIESPublicKeyEncoder());

        iesEngine.init(new ECPublicKeyParameters(pub, CURVE), parametersWithIV, ephemeralKeyPairGenerator);

        return iesEngine.processBlock(plaintext, 0, plaintext.length);
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

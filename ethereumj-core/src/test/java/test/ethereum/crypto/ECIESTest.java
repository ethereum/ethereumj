package test.ethereum.crypto;

import org.ethereum.ConcatKDFBytesGenerator;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.EthereumIESEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.BufferedBlockCipher;
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
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ECIESTest {
    public static final int MAC_KEY_SIZE = 128;
    static Logger log = LoggerFactory.getLogger("test");
    private static ECDomainParameters curve;
    private static final String CIPHERTEXT1 = "042a851331790adacf6e64fcb19d0872fcdf1285a899a12cdc897da941816b0ea6485402aaf6c2e0a5d98ae3af1b05c68b307d1e0eb7a426a46f1617ba5b94f90b606eee3b5e9d2b527a9ee52cfa377bcd118b9390ed27ffe7d48e8155004375cae209012c3e057bb13a478a64a201d79ad4ae83";
    private static final X9ECParameters IES_CURVE_PARAM = SECNamedCurves.getByName("secp256r1");
    private static final BigInteger PRIVATE_KEY1 = new BigInteger("51134539186617376248226283012294527978458758538121566045626095875284492680246");

    private static ECPoint pub(BigInteger d) throws Exception {
        return curve.getG().multiply(d);
    }

    @BeforeClass
    public static void beforeAll() {
        if (Security.getProvider("SC") == null)
            Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        curve = new ECDomainParameters(IES_CURVE_PARAM.getCurve(), IES_CURVE_PARAM.getG(), IES_CURVE_PARAM.getN(), IES_CURVE_PARAM.getH());
    }

    @Test
    public void testKDF() {
        ConcatKDFBytesGenerator kdf = new ConcatKDFBytesGenerator(new SHA256Digest());
        kdf.init(new KDFParameters(new String("Hello").getBytes(), new byte[0]));
        byte[] bytes = new byte[2];
        kdf.generateBytes(bytes, 0, bytes.length);
        assertArrayEquals(new byte[]{-66, -89}, bytes);
    }

    @Test
    public void testDecryptTestVector() throws Throwable {
        ECPoint pub1 = pub(PRIVATE_KEY1);
        byte[] cipher = Hex.decode(CIPHERTEXT1);
        ByteArrayInputStream is = new ByteArrayInputStream(cipher);
        byte[] ephemBytes = new byte[2*((curve.getCurve().getFieldSize()+7)/8) + 1];
        is.read(ephemBytes);
        ECPoint ephem = curve.getCurve().decodePoint(ephemBytes);
        byte[] IV = new byte[MAC_KEY_SIZE/8];
        is.read(IV);
        byte[] cipherBody = new byte[is.available()];
        is.read(cipherBody);

        byte[] plaintext = decrypt(ephem, PRIVATE_KEY1, IV, cipherBody);
        assertArrayEquals(new byte[]{1,1,1}, plaintext);
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

        iesEngine.init(false, new ECPrivateKeyParameters(prv, curve), new ECPublicKeyParameters(ephem, curve), parametersWithIV);

        byte[] message = iesEngine.processBlock(cipher, 0, cipher.length);
        return message;
    }

    public static byte[] encrypt(byte[] plaintext) throws Throwable {
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
        KeyGenerationParameters gParam = new ECKeyGenerationParameters(ECKey.CURVE, random);

        eGen.init(gParam);


        AsymmetricCipherKeyPair p1 = eGen.generateKeyPair();
        AsymmetricCipherKeyPair p2 = eGen.generateKeyPair();


        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(ECKey.CURVE, random);
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keygenParams);

        ECKeyPairGenerator gen = new ECKeyPairGenerator();
        gen.init(new ECKeyGenerationParameters(ECKey.CURVE, random));

        iesEngine.init(true, p1.getPrivate(), p2.getPublic(), parametersWithIV);

        byte[] cipher = iesEngine.processBlock(plaintext, 0, plaintext.length);
        return cipher;
    }

}

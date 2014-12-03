package org.ethereum.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.crypto.ECKey;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EtherSaleWalletDecoderTest {

    private ObjectMapper mapper = new ObjectMapper();
    private EtherSaleWalletDecoder walletDecoder;
    private EtherSaleWallet etherSaleWallet;

    @Before
    public void setUp() throws IOException {
        etherSaleWallet = mapper.readValue(getClass().getResourceAsStream("/wallet/ethersalewallet.json"), EtherSaleWallet.class);
        walletDecoder = new EtherSaleWalletDecoder(etherSaleWallet);
    }

    @Test
    public void shouldGeneratePasswordHash() throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] result = walletDecoder.generatePasswordHash("foobar");
        String resultString = Hex.toHexString(result);

        assertThat(resultString.toLowerCase(), is("d11d0652ee9ca94500ba8301903c8f6a"));
    }

    @Test
    public void shouldGeneratePasswordHashWithUmlauts() throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] result = walletDecoder.generatePasswordHash("öäüß");
        String resultString = Hex.toHexString(result);

        assertThat(resultString.toLowerCase(), is("67162c127acd9ac55a75a8c5367b9a1a"));
    }

    @Test
    public void shouldGeneratePasswordHashWithUnicode() throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] result = walletDecoder.generatePasswordHash("☯");
        String resultString = Hex.toHexString(result);

        assertThat(resultString.toLowerCase(), is("47204606123eae746a633c632904d94f"));
    }

    @Test
    public void shouldDecryptSeed() throws InvalidCipherTextException {
        byte[] result = walletDecoder.decryptSeed(walletDecoder.generatePasswordHash("foobar"), etherSaleWallet.getEncseedBytes());
        String resultString = Hex.toHexString(result);

        assertThat(resultString.toLowerCase(), is("37343165366130323566656533363039626262613564366430373038353964643534623862646231653232333431363133653462623832643333313537663035"));
    }

    @Test
    public void shouldGetPrivateKey() throws InvalidCipherTextException {
        byte[] result = walletDecoder.getPrivateKey("foobar");
        String resultString = Hex.toHexString(result);

        assertThat(resultString.toLowerCase(), is("74ef8a796480dda87b4bc550b94c408ad386af0f65926a392136286784d63858"));
    }

    @Test(expected = InvalidCipherTextException.class)
    public void shouldRejectWrongPassword() throws InvalidCipherTextException {
        walletDecoder.getPrivateKey("foo");
    }

    @Test(expected = InvalidCipherTextException.class)
    public void shouldRejectWrongPasswordSameLength() throws InvalidCipherTextException {
        walletDecoder.getPrivateKey("barfoo");
    }

    @Test
    public void ethereumAddressShouldMatchPrivateKey() throws InvalidCipherTextException {
        BigInteger privKey = new BigInteger(walletDecoder.getPrivateKey("foobar"));
        byte[] addr = ECKey.fromPrivate(privKey).getAddress();
        assertThat(Hex.toHexString(etherSaleWallet.getEthaddrBytes()), is(Hex.toHexString(addr)));
    }

    @Test(expected = InvalidCipherTextException.class)
    public void shouldHandleBrokenWallet() throws IOException, InvalidCipherTextException {
        EtherSaleWallet brokenEtherSaleWallet = mapper.readValue(getClass().getResourceAsStream("/wallet/ethersalewallet_broken.json"), EtherSaleWallet.class);
        EtherSaleWalletDecoder walletDecoder = new EtherSaleWalletDecoder(brokenEtherSaleWallet);
        walletDecoder.getPrivateKey("foobar");
    }
}

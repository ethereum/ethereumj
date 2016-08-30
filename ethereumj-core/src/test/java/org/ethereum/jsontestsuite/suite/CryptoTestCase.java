package org.ethereum.jsontestsuite.suite;

import org.ethereum.crypto.ECIESCoder;
import org.ethereum.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 08.02.2015
 */
public class CryptoTestCase {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    private String decryption_type = "";
    private String key = "";
    private String cipher = "";
    private String payload = "";


    public CryptoTestCase(){
    }


    public void execute(){

        byte[] key = Hex.decode(this.key);
        byte[] cipher = Hex.decode(this.cipher);

        ECKey ecKey = ECKey.fromPrivate(key);

        byte[] resultPayload = new byte[0];
        if (decryption_type.equals("aes_ctr"))
            resultPayload = ecKey.decryptAES(cipher);

        if (decryption_type.equals("ecies_sec1_altered"))
            try {
                resultPayload = ECIESCoder.decrypt(new BigInteger(Hex.toHexString(key), 16), cipher);
            } catch (Throwable e) {e.printStackTrace();}

        if (!Hex.toHexString(resultPayload).equals(payload)){
            String error = String.format("payload should be: %s, but got that result: %s  ",
                    payload, Hex.toHexString(resultPayload));
            logger.info(error);

            System.exit(-1);
        }
    }


    public String getDecryption_type() {
        return decryption_type;
    }

    public void setDecryption_type(String decryption_type) {
        this.decryption_type = decryption_type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "CryptoTestCase{" +
                "decryption_type='" + decryption_type + '\'' +
                ", key='" + key + '\'' +
                ", cipher='" + cipher + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}

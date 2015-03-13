package org.ethereum.jsontestsuite;

import org.ethereum.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

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
        byte[] resultPayload = ecKey.decryptAES(cipher);

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

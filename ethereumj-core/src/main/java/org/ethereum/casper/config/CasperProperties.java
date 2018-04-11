package org.ethereum.casper.config;

import org.ethereum.config.SystemProperties;
import org.ethereum.util.ByteUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.ethereum.casper.config.net.CasperTestConfig.EPOCH_LENGTH;

public class CasperProperties extends SystemProperties {

    private static CasperProperties CONFIG;

    private byte[] casperAddress = null;

    public static SystemProperties getDefault() {
        if (CONFIG == null) {
            CONFIG = new CasperProperties();
        }
        return CONFIG;
    }

    public byte[] getCasperAddress() {
        return casperAddress;
    }

    public void setCasperAddress(byte[] casperAddress) {
        this.casperAddress = casperAddress;
    }

    public int getCasperEpochLength() {
        return EPOCH_LENGTH;
    }

    public byte[] getCasperValidatorPrivateKey() {
        String key = config.getString("casper.validator.privateKey");
        if (key == null) return null;
        return ByteUtil.hexStringToBytes(key);
    }

    public long getCasperValidatorDeposit() {
        return config.getLong("casper.validator.deposit");
    }

    public Boolean getCasperValidatorEnabled() {
        return config.getBoolean("casper.validator.enabled");
    }

    public String getCasperAbi() {
        final String abiLocation = config.getString("casper.contractAbi");
        return readFile(abiLocation);
    }

    public String getCasperBin() {
        final String binLocation = config.getString("casper.contractBin");
        return readFile(binLocation);
    }

    private static String readFile(final String location) {
        try {
            InputStream is = SystemProperties.class.getResourceAsStream(location);

            if (is != null) {
                return readStream(is);
            } else {
                logger.error("File not found `{}`", location);
                throw new RuntimeException(String.format("File not found `%s`", location));
            }
        } catch (Exception ex) {
            String errorMsg = String.format("Error while reading file from %s", location);
            logger.error(errorMsg, ex);
            throw new RuntimeException(errorMsg, ex);
        }
    }

    private static String readStream(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}

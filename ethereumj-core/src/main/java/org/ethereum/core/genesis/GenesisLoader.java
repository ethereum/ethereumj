package org.ethereum.core.genesis;

import com.google.common.io.ByteStreams;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Genesis;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.core.Genesis.ZERO_HASH_2048;
import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.util.ByteUtil.*;
import static org.ethereum.core.BlockHeader.NONCE_LENGTH;

public class GenesisLoader {

    /**
     * Load genesis from passed location or from classpath `genesis` directory
     */
    public static GenesisJson loadGenesisJson(SystemProperties config, ClassLoader classLoader) throws RuntimeException {
        final String genesisFile = config.getProperty("genesisFile", null);
        final String genesisResource = config.genesisInfo();

        // #1 try to find genesis at passed location
        if (genesisFile != null) {
            try (InputStream is = new FileInputStream(new File(genesisFile))) {
                return loadGenesisJson(is);
            } catch (Exception e) {
                showLoadError("Problem loading " + genesisFile, genesisFile, genesisResource);
            }
        }

        // #2 fall back to old genesis location at `src/main/resources/genesis` directory
        InputStream is = classLoader.getResourceAsStream("genesis/" + genesisResource);
        if (is != null) {
            try {
                return loadGenesisJson(is);
            } catch (Exception e) {
                showGenesisErrorAndExit("Problem loading genesis file from resource directory", genesisFile, genesisResource);
            }
        } else {
            showGenesisErrorAndExit("Genesis file is not found in resource directory", genesisFile, genesisResource);
        }

        return null;
    }

    private static void showLoadError(String message, String genesisFile, String genesisResource) {
        showGenesisErrorAndExit(
            message,
            "Config option 'genesisFile': " + genesisFile,
            "Config option 'genesis': " + genesisResource);
    }

    private static void showGenesisErrorAndExit(String message, String... messages) {
        LoggerFactory.getLogger("general").error(message);

        System.err.println("");
        System.err.println("");
        for (String msg : messages) {
            System.err.println(msg);
        }
        System.err.println(message);
        System.err.println("");
        System.err.println("");

        // hope to remove this
        throw new RuntimeException("Wasn't able to load genesis. " + message);
    }

    public static Genesis parseGenesis(BlockchainNetConfig blockchainNetConfig, GenesisJson genesisJson) throws RuntimeException {
        try {
            Genesis genesis = createBlockForJson(genesisJson);

            Map<ByteArrayWrapper, AccountState> premine = generatePreMine(blockchainNetConfig, genesisJson.getAlloc());
            genesis.setPremine(premine);

            byte[] rootHash = generateRootHash(premine);
            genesis.setStateRoot(rootHash);

            return genesis;
        } catch (Exception e) {
            e.printStackTrace();
            showGenesisErrorAndExit("Problem parsing genesis", e.getMessage());
        }
        return null;
    }

    /**
     * Method used much in tests.
     */
    public static Genesis loadGenesis(InputStream resourceAsStream) {
        GenesisJson genesisJson = loadGenesisJson(resourceAsStream);
        return parseGenesis(SystemProperties.getDefault().getBlockchainConfig(), genesisJson);
    }

    public static GenesisJson loadGenesisJson(InputStream genesisJsonIS) throws RuntimeException {
        try {
            String json = new String(ByteStreams.toByteArray(genesisJsonIS));

            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructType(GenesisJson.class);

            GenesisJson genesisJson  = new ObjectMapper().readValue(json, type);
            return genesisJson;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private static Genesis createBlockForJson(GenesisJson genesisJson) {

        byte[] nonce       = prepareNonce(ByteUtil.hexStringToBytes(genesisJson.nonce));
        byte[] difficulty  = ByteUtil.hexStringToBytes(genesisJson.difficulty);
        byte[] mixHash     = ByteUtil.hexStringToBytes(genesisJson.mixhash);
        byte[] coinbase    = ByteUtil.hexStringToBytes(genesisJson.coinbase);

        byte[] timestampBytes = ByteUtil.hexStringToBytes(genesisJson.timestamp);
        long   timestamp         = ByteUtil.byteArrayToLong(timestampBytes);

        byte[] parentHash  = ByteUtil.hexStringToBytes(genesisJson.parentHash);
        byte[] extraData   = ByteUtil.hexStringToBytes(genesisJson.extraData);

        byte[] gasLimitBytes    = ByteUtil.hexStringToBytes(genesisJson.gasLimit);
        long   gasLimit         = ByteUtil.byteArrayToLong(gasLimitBytes);

        return new Genesis(parentHash, EMPTY_LIST_HASH, coinbase, ZERO_HASH_2048,
                            difficulty, 0, gasLimit, 0, timestamp, extraData,
                            mixHash, nonce);
    }

    /**
     * Prepares nonce to be correct length
     * @param nonceUnchecked    unchecked, user-provided nonce
     * @return  correct nonce
     * @throws RuntimeException when nonce is too long
     */
    private static byte[] prepareNonce(byte[] nonceUnchecked) {
        if (nonceUnchecked.length > 8) {
            throw new RuntimeException(String.format("Invalid nonce, should be %s length", NONCE_LENGTH));
        } else if (nonceUnchecked.length == 8) {
            return nonceUnchecked;
        }
        byte[] nonce = new byte[NONCE_LENGTH];
        int diff = NONCE_LENGTH - nonceUnchecked.length;
        for (int i = diff; i < NONCE_LENGTH; ++i) {
            nonce[i] = nonceUnchecked[i - diff];
        }
        return nonce;
    }


    private static Map<ByteArrayWrapper, AccountState> generatePreMine(BlockchainNetConfig blockchainNetConfig, Map<String, AllocatedAccount> alloc){

        Map<ByteArrayWrapper, AccountState> premine = new HashMap<>();
        for (String key : alloc.keySet()){

            final String rawBalance = alloc.get(key).getBalance();
            final BigInteger balance;
            if (rawBalance != null && rawBalance.startsWith("0x")) {
                // hex passed
                balance = bytesToBigInteger(hexStringToBytes(rawBalance));
            } else {
                // decimal passed
                balance = new BigInteger(rawBalance);
            }
            AccountState acctState = new AccountState(
                    blockchainNetConfig.getCommonConstants().getInitialNonce(), balance);

            premine.put(wrap(hexStringToBytes(key)), acctState);
        }

        return premine;
    }

    private static byte[] generateRootHash(Map<ByteArrayWrapper, AccountState> premine){

        Trie<byte[]> state = new SecureTrie((byte[]) null);

        for (ByteArrayWrapper key : premine.keySet()) {
            state.put(key.getData(), premine.get(key).getEncoded());
        }

        return state.getRootHash();
    }
}

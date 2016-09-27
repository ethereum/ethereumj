package org.ethereum.core.genesis;

import com.google.common.io.ByteStreams;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Genesis;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.core.Genesis.ZERO_HASH_2048;
import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.util.ByteUtil.wrap;

public class GenesisLoader {

    /**
     * Load genesis from passed location or from classpath `genesis` directory
     */
    public static Genesis loadGenesis(SystemProperties config, ClassLoader classLoader) throws RuntimeException {
        final String genesisFile = config.getProperty("genesisFile", null);
        final String genesisResource = config.genesisInfo();

        // #1 try to find genesis at passed location
        if (genesisFile != null) {
            try (InputStream is = new FileInputStream(new File(genesisFile))) {
                return GenesisLoader.loadGenesis(config, is);
            } catch (Exception e) {
                showGenesisErrorAndExit("Problem loading " + genesisFile, genesisFile, genesisResource);
            }
        }

        // #2 fall back to old genesis location at `src/main/resources/genesis` directory
        try {
            InputStream is = classLoader.getResourceAsStream("genesis/" + genesisResource);
            if (is != null) {
                return loadGenesis(config, is);
            } else {
                showGenesisErrorAndExit("Genesis file is not found in resource directory", genesisFile, genesisResource);
            }
        } catch (Exception e) {
            showGenesisErrorAndExit("Problem loading genesis file from resource directory", genesisFile, genesisResource);
        }
        return null;
    }

    private static void showGenesisErrorAndExit(String message, String genesisFile, String genesisResource) {
        System.err.println("");
        System.err.println("");
        System.err.println("Genesis block configuration is corrupted or not found.");
        System.err.println("Checked option 'genesisFile': " + genesisFile);
        System.err.println("Checked option 'genesis': " + genesisResource);
        System.err.println(message);

        // hope to remove this
        System.exit(-1);
//        throw new Error("Wan't able to load genesis at " + genesisFile, exception1);
    }

    /**
     * Method used much in tests.
     */
    public static Genesis loadGenesis(InputStream genesisJsonIS) throws RuntimeException {
        try {
            return loadGenesis(SystemProperties.getDefault(), genesisJsonIS);
        } catch (Exception e) {
            System.err.println("Genesis block configuration is corrupted or not found");
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    private static Genesis loadGenesis(SystemProperties config, InputStream genesisJsonIS) throws RuntimeException {
        try {
            String json = new String(ByteStreams.toByteArray(genesisJsonIS));

            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructType(GenesisJson.class);

            GenesisJson genesisJson  = new ObjectMapper().readValue(json, type);

            Genesis genesis = createBlockForJson(genesisJson);

            Map<ByteArrayWrapper, AccountState> premine = generatePreMine(config, genesisJson.getAlloc());
            genesis.setPremine(premine);

            byte[] rootHash = generateRootHash(premine);
            genesis.setStateRoot(rootHash);

            return genesis;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private static Genesis createBlockForJson(GenesisJson genesisJson){

        byte[] nonce       = ByteUtil.hexStringToBytes(genesisJson.nonce);
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


    private static Map<ByteArrayWrapper, AccountState> generatePreMine(SystemProperties config, Map<String, AllocatedAccount> alloc){

        Map<ByteArrayWrapper, AccountState> premine = new HashMap<>();
        for (String key : alloc.keySet()){

            BigInteger balance = new BigInteger(alloc.get(key).getBalance());
            AccountState acctState = new AccountState(
                    config.getBlockchainConfig().getCommonConstants().getInitialNonce(), balance);

            premine.put(wrap(Hex.decode(key)), acctState);
        }

        return premine;
    }

    private static byte[] generateRootHash(Map<ByteArrayWrapper, AccountState> premine){

        Trie state = new SecureTrie(null);

        for (ByteArrayWrapper key : premine.keySet()) {
            state.update(key.getData(), premine.get(key).getEncoded());
        }

        return state.getRootHash();
    }

}

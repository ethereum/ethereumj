package org.ethereum.core.genesis;

import com.google.common.io.ByteStreams;
import com.typesafe.config.Config;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.config.blockchain.OlympicConfig;
import org.ethereum.config.net.*;
import org.ethereum.core.AccountState;
import org.ethereum.core.Genesis;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.CollectionUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.core.Genesis.ZERO_HASH_2048;
import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.util.ByteUtil.wrap;
import static org.ethereum.core.BlockHeader.NONCE_LENGTH;

public class GenesisLoader {

    /**
     * Load genesis from passed location or from classpath `genesis` directory
     */
    public static Pair<Genesis, BlockchainNetConfig> loadGenesis(SystemProperties config, ClassLoader classLoader) throws RuntimeException {
        final String genesisFile = config.getProperty("genesisFile", null);
        final String genesisResource = config.genesisInfo();

        // #1 try to find genesis at passed location
        if (genesisFile != null) {
            try (InputStream is = new FileInputStream(new File(genesisFile))) {
                return GenesisLoader.loadGenesis(config, is, classLoader);
            } catch (Exception e) {
                showGenesisErrorAndExit("Problem loading " + genesisFile, genesisFile, genesisResource);
            }
        }

        // #2 fall back to old genesis location at `src/main/resources/genesis` directory
        try {
            InputStream is = classLoader.getResourceAsStream("genesis/" + genesisResource);
            if (is != null) {
                return loadGenesis(config, is, classLoader);
            } else {
                showGenesisErrorAndExit("Genesis file is not found in resource directory", genesisFile, genesisResource);
            }
        } catch (Exception e) {
            showGenesisErrorAndExit("Problem loading genesis file from resource directory", genesisFile, genesisResource);
        }
        return null;
    }

    private static void showGenesisErrorAndExit(String message, String genesisFile, String genesisResource) {
        LoggerFactory.getLogger("general").error(message);

        System.err.println("");
        System.err.println("");
        System.err.println("Genesis block configuration is corrupted or not found.");
        System.err.println("Config option 'genesisFile': " + genesisFile);
        System.err.println("Config option 'genesis': " + genesisResource);
        System.err.println(message);
        System.err.println("");
        System.err.println("");

        // hope to remove this
        throw new RuntimeException("Wasn't able to load genesis. " + message);
    }

    /**
     * Method used much in tests.
     */
    public static Genesis loadGenesis(InputStream genesisJsonIS) throws RuntimeException {
        try {
            return loadGenesis(SystemProperties.getDefault(), genesisJsonIS, GenesisLoader.class.getClassLoader()).getLeft();
        } catch (Exception e) {
            System.err.println("Genesis block configuration is corrupted or not found");
            e.printStackTrace();
            throw new RuntimeException("Wasn't able to load genesis");
        }
    }

    private static Pair<Genesis, BlockchainNetConfig> loadGenesis(SystemProperties config, InputStream genesisJsonIS, ClassLoader classLoader) throws RuntimeException {
        try {
            String json = new String(ByteStreams.toByteArray(genesisJsonIS));

            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructType(GenesisJson.class);

            GenesisJson genesisJson = new ObjectMapper().readValue(json, type);

            BlockchainNetConfig blockchainNetConfig = loadBlockchainNetConfig(config.getConfig(), classLoader, genesisJson.config);

            Genesis genesis = createBlockForJson(genesisJson);

            Map<ByteArrayWrapper, AccountState> premine = generatePreMine(genesisJson.getAlloc(), blockchainNetConfig);
            genesis.setPremine(premine);

            byte[] rootHash = generateRootHash(premine);
            genesis.setStateRoot(rootHash);

            return Pair.of(genesis, blockchainNetConfig);
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


    private static Map<ByteArrayWrapper, AccountState> generatePreMine(Map<String, AllocatedAccount> alloc, BlockchainNetConfig blockchainNetConfig){

        Map<ByteArrayWrapper, AccountState> premine = new HashMap<>();
        for (String key : alloc.keySet()){

            BigInteger balance = new BigInteger(alloc.get(key).getBalance());
            AccountState acctState = new AccountState(
                    blockchainNetConfig.getCommonConstants().getInitialNonce(), balance);

            premine.put(wrap(ByteUtil.hexStringToBytes(key)), acctState);
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

    public static BlockchainNetConfig loadBlockchainNetConfig(Config config, ClassLoader classLoader, Map<String, String> rawConfig) {
        BlockchainNetConfig blockchainConfig;
        if (rawConfig != null && rawConfig.size() > 0) {
            return AbstractNetConfig.fromGenesisConfig(rawConfig);
        }

        if (config.hasPath("blockchain.config.name") && config.hasPath("blockchain.config.class")) {
            throw new RuntimeException("Only one of two options should be defined: 'blockchain.config.name' and 'blockchain.config.class'");
        }
        if (config.hasPath("blockchain.config.name")) {
            switch(config.getString("blockchain.config.name")) {
                case "main":
                    blockchainConfig = new MainNetConfig();
                    break;
                case "olympic":
                    blockchainConfig = new OlympicConfig();
                    break;
                case "morden":
                    blockchainConfig = new MordenNetConfig();
                    break;
                case "ropsten":
                    blockchainConfig = new RopstenNetConfig();
                    break;
                case "testnet":
                    blockchainConfig = new TestNetConfig();
                    break;
                default:
                    throw new RuntimeException("Unknown value for 'blockchain.config.name': '" + config.getString("blockchain.config.name") + "'");
            }
        } else {
            String className = config.getString("blockchain.config.class");
            try {
                Class<? extends BlockchainNetConfig> aClass = (Class<? extends BlockchainNetConfig>) classLoader.loadClass(className);
                blockchainConfig = aClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("The class specified via blockchain.config.class '" + className + "' not found" , e);
            } catch (ClassCastException e) {
                throw new RuntimeException("The class specified via blockchain.config.class '" + className + "' is not instance of org.ethereum.config.BlockchainForkConfig" , e);
            } catch (InstantiationException|IllegalAccessException e) {
                throw new RuntimeException("The class specified via blockchain.config.class '" + className + "' couldn't be instantiated (check for default constructor and its accessibility)" , e);
            }
        }
        return blockchainConfig;
    }

}

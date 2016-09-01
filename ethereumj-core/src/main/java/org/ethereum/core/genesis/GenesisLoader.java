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

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.core.Genesis.ZERO_HASH_2048;
import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.util.ByteUtil.wrap;

public class GenesisLoader {

    public static Genesis loadGenesis(SystemProperties config, ClassLoader classLoader)  {
        String genesisFile = config.genesisInfo();

        InputStream is = classLoader.getResourceAsStream("genesis/" + genesisFile);
        return loadGenesis(config, is);
    }

    public static Genesis loadGenesis(InputStream genesisJsonIS) {
        return loadGenesis(SystemProperties.getDefault(), genesisJsonIS);
    }
    
    public static Genesis loadGenesis(SystemProperties config, InputStream genesisJsonIS)  {
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
        } catch (Throwable e) {
            System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
            e.printStackTrace();
            System.exit(-1);
        }

        System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
        System.exit(-1);
        return null;
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

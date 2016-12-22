package org.ethereum.config.net;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.config.Constants;
import org.ethereum.config.blockchain.*;
import org.ethereum.util.CollectionUtils;

import java.util.*;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class AbstractNetConfig implements BlockchainNetConfig {
    private long[] blockNumbers = new long[64];
    private BlockchainConfig[] configs = new BlockchainConfig[64];
    private int count;

    public void add(long startBlockNumber, BlockchainConfig config) {
        if (count >= blockNumbers.length) throw new RuntimeException();
        if (count > 0 && blockNumbers[count] >= startBlockNumber)
            throw new RuntimeException("Block numbers should increase");
        if (count == 0 && startBlockNumber > 0) throw new RuntimeException("First config should start from block 0");
        blockNumbers[count] = startBlockNumber;
        configs[count] = config;
        count++;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockNumber) {
        for (int i = 0; i < count; i++) {
            if (blockNumber < blockNumbers[i]) return configs[i - 1];
        }
        return configs[count - 1];
    }

    @Override
    public Constants getCommonConstants() {
        // TODO make a guard wrapper which throws exception if the requested constant differs among configs
        return configs[0].getConstants();
    }

    /**
     * Convert JSON config from genesis to Java blockchain net config.
     */
    public static BlockchainNetConfig fromGenesisConfig(Map<String, String> config) throws RuntimeException {
        AbstractNetConfig netConfig = new AbstractNetConfig();

        final List<String> keys = Arrays.asList("homesteadBlock", "daoForkBlock", "EIP150Block", "EIP155Block", "EIP158Block");
        final ArrayList<Integer> orderedBlocks = new ArrayList<>();
        final HashMap<Integer, String> blockToKey = new HashMap<>();

        // #1 create block number to config map
        for (String key : keys) {
            Integer blockNumber = getInteger(config, key);
            if (blockNumber != null) {
                if (orderedBlocks.contains(blockNumber)) {
                    throw new RuntimeException("Genesis net config contains duplicate blocks");
                }
                orderedBlocks.add(blockNumber);
                blockToKey.put(blockNumber, key);
            }
        }

        // #2 sort block numbers before adding configs
        Collections.sort(orderedBlocks);

        // hardcoded initial config
        Integer prevBlockNumber = 0;    // frontier block
        netConfig.add(0, new FrontierConfig());

        // #3 fill configs in proper order
        for (Integer blockNumber : orderedBlocks) {
            String key = blockToKey.get(blockNumber);
            switch (key) {
                case "homesteadBlock":
                    netConfig.add(blockNumber, new HomesteadConfig());
                    break;
                case "daoForkBlock":
                    if ("true".equalsIgnoreCase(config.get("daoForkSupport"))) {
                        netConfig.add(blockNumber, new DaoHFConfig().withForkBlock(blockNumber));
                    } else {
                        netConfig.add(blockNumber, new DaoNoHFConfig().withForkBlock(blockNumber));
                    }
                    break;
                case "EIP150Block":
                    netConfig.add(blockNumber, new Eip150HFConfig(netConfig.getConfigForBlock(prevBlockNumber)));
                    break;
                // TODO handle other EIP configs
                default:
                    continue;
            }
            prevBlockNumber = blockNumber;
        }

        return netConfig;
    }

    private static Integer getInteger(Map<String, String> config, String key) {
        return config.containsKey(key) ? Integer.parseInt(config.get(key)) : null;
    }
}

package org.ethereum.config.net;

import org.ethereum.config.blockchain.*;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Convert JSON config from genesis to Java blockchain net config.
 * Created by Stan Reshetnyk on 23.12.2016.
 */
public class JsonNetConfig extends BaseNetConfig {

    public JsonNetConfig(Map<String, String> config) throws RuntimeException {
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
        Integer prevBlockNumber = 0;
        // add configuration for first block if not explicitly passed
        if (orderedBlocks.get(0) > 0) {
            add(0, new FrontierConfig());
        }

        // #3 fill configs in proper order
        for (Integer blockNumber : orderedBlocks) {
            final String key = blockToKey.get(blockNumber);
            switch (key) {
                case "homesteadBlock":
                    add(blockNumber, new HomesteadConfig());
                    break;
                case "daoForkBlock":
                    if ("true".equalsIgnoreCase(config.get("daoForkSupport"))) {
                        add(blockNumber, new DaoHFConfig(getConfigForBlock(prevBlockNumber), blockNumber));
                    } else {
                        add(blockNumber, new DaoNoHFConfig(getConfigForBlock(prevBlockNumber), blockNumber));
                    }
                    break;
                case "EIP150Block":
                    if (blockNumber == 0) {
                        throw new RuntimeException("Unexpected and untested blockchain configuration");
                    }
                    add(blockNumber, new Eip150HFConfig(getConfigForBlock(prevBlockNumber)));
                    break;
                // TODO handle other EIP configs
                default:
                    LoggerFactory.getLogger("general").warn("Ignored config option from genesis {}: {}", blockNumber, key);
                    continue;
            }
            prevBlockNumber = blockNumber;
        }
    }

    private static Integer getInteger(Map<String, String> config, String key) {
        return config.containsKey(key) ? Integer.parseInt(config.get(key)) : null;
    }

}

package org.ethereum.config.net;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.blockchain.*;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Convert JSON config from genesis to Java blockchain net config.
 * Created by Stan Reshetnyk on 23.12.2016.
 */
public class JsonNetConfig extends BaseNetConfig {

    private final String HOMESTEAD_BLOCK = "homesteadBlock".toLowerCase();
    private final String DAO_FORK_BLOCK = "daoForkBlock".toLowerCase();
    private final String EIP150_BLOCK = "eip150Block".toLowerCase();
    private final String EIP_155_BLOCK = "eip155Block".toLowerCase();
    private final String EIP_158_BLOCK = "eip158Block".toLowerCase();

    final List<String> keys = Arrays.asList(DAO_FORK_BLOCK, EIP150_BLOCK, EIP_155_BLOCK, EIP_158_BLOCK);

    final BlockchainConfig initialBlockConfig = new FrontierConfig();

    /**
     * We convert all string keys to lowercase before processing.
     *
     * Homestead block is 0 if not specified.
     * If Homestead block is specified then Frontier will be used for 0 block.
     *
     * @param configValue
     */
    public JsonNetConfig(Map<String, String> configValue) throws RuntimeException {
        // force `configs` values to lowercase
        final Map<String, String> config = new HashMap<>();
        if (configValue != null) {
            for (String key : configValue.keySet()) {
                config.put(key.toLowerCase(), configValue.get(key));
            }
        }

        final List<Pair<Integer, BlockchainConfig>> candidates = new ArrayList<>();

        {
            Pair<Integer, BlockchainConfig> lastCandidate = Pair.of(0, initialBlockConfig);
            candidates.add(lastCandidate);

            // homestead block assumed to be 0
            final Integer homesteadBlockSpecified = getInteger(config, HOMESTEAD_BLOCK);
            final int homesteadBlock = homesteadBlockSpecified == null ? 0 : homesteadBlockSpecified;
            lastCandidate = Pair.of(0, getBlockchainConfigCandidate(config, HOMESTEAD_BLOCK, homesteadBlock, lastCandidate));
            candidates.add(lastCandidate);

            // preparation
            // we have single config for EIP155 and EIP158
            // remove one of them and hope they sits on the same block
            final String eip155Block = config.remove(EIP_155_BLOCK);
            if (eip155Block != null) {
                if (config.containsKey(EIP_158_BLOCK) && eip155Block.equalsIgnoreCase(config.get(EIP_158_BLOCK))) {
                    // all fine, we can handle this
                } else {
                    LoggerFactory.getLogger("general").warn("Ignored config option EIP155Block: {}", eip155Block);
                }
            }

            // fill candidates allowing multiple candidates to have same block number
            for (String key : keys) {
                final Integer blockNumber = getInteger(config, key);
                if (blockNumber == null) {
                    continue;
                }
                if (blockNumber.compareTo(lastCandidate.getLeft()) < 0) {
                    throw new RuntimeException(String.format("Detected invalid network config setup. Config %s:%d cant be lower %s at block %d",
                            key, blockNumber, lastCandidate.getRight().toString(), lastCandidate.getLeft()));
                }
                final BlockchainConfig candidate = getBlockchainConfigCandidate(config, key, blockNumber, lastCandidate);
                if (candidate != null) {
                    lastCandidate = Pair.of(blockNumber, candidate);
                    candidates.add(lastCandidate);
                }
            }
        }

        {
            // add candidate per each block (take last in row for same block)
            Pair<Integer, BlockchainConfig> last = candidates.remove(0);
            for (Pair<Integer, BlockchainConfig> current : candidates) {
                if (current.getLeft().compareTo(last.getLeft()) > 0) {
                    add(last.getLeft(), last.getRight());
                }
                last = current;
            }
            add(last.getLeft(), last.getRight());
        }
    }

    private BlockchainConfig getBlockchainConfigCandidate(Map<String, String> config, String key, Integer blockNumber, Pair<Integer, BlockchainConfig> previous) {
        final BlockchainConfig prevChain = previous.getRight();

        if (HOMESTEAD_BLOCK.equals(key)) {
            return new HomesteadConfig();

        } else if (DAO_FORK_BLOCK.equals(key)) {
            if ("true".equalsIgnoreCase(config.get("daoForkSupport".toLowerCase()))) {
                return new DaoHFConfig(previous.getRight(), blockNumber);
            } else {
                return new DaoNoHFConfig(prevChain, blockNumber);
            }

        } else if (EIP150_BLOCK.equals(key)) {
            return new Eip150HFConfig(prevChain);

        } else if (EIP_158_BLOCK.equals(key)) {
            return new Eip160HFConfig(prevChain);
        }

        LoggerFactory.getLogger("general").warn("Ignored config option from genesis {}: {}", blockNumber, key);
        return null;
    }

    private static Integer getInteger(Map<String, String> config, String key) {
        return config.containsKey(key) ? Integer.parseInt(config.get(key)) : null;
    }

}

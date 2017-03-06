package org.ethereum.config.net;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.blockchain.*;
import org.ethereum.core.genesis.GenesisConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert JSON config from genesis to Java blockchain net config.
 * Created by Stan Reshetnyk on 23.12.2016.
 */
public class JsonNetConfig extends BaseNetConfig {

    final BlockchainConfig initialBlockConfig = new FrontierConfig();

    /**
     * We convert all string keys to lowercase before processing.
     *
     * Homestead block is 0 if not specified.
     * If Homestead block is specified then Frontier will be used for 0 block.
     *
     * @param config
     */
    public JsonNetConfig(GenesisConfig config) throws RuntimeException {

        final List<Pair<Integer, ? extends BlockchainConfig>> candidates = new ArrayList<>();

        {
            Pair<Integer, ? extends BlockchainConfig> lastCandidate = Pair.of(0, initialBlockConfig);
            candidates.add(lastCandidate);

            // homestead block assumed to be 0 by default
            lastCandidate = Pair.of(config.homesteadBlock == null ? 0 : config.homesteadBlock, new HomesteadConfig());
            candidates.add(lastCandidate);

            if (config.daoForkBlock != null) {
                AbstractDaoConfig daoConfig = config.daoForkSupport ?
                        new DaoHFConfig(lastCandidate.getRight(), config.daoForkBlock) :
                        new DaoNoHFConfig(lastCandidate.getRight(), config.daoForkBlock);
                lastCandidate = Pair.of(config.daoForkBlock, daoConfig);
                candidates.add(lastCandidate);
            }

            if (config.eip150Block != null) {
                lastCandidate = Pair.of(config.eip150Block, new Eip150HFConfig(lastCandidate.getRight()));
                candidates.add(lastCandidate);
            }

            if (config.eip155Block != null || config.eip158Block != null) {
                int block;
                if (config.eip155Block != null) {
                    if (config.eip158Block != null && !config.eip155Block.equals(config.eip158Block)) {
                        throw new RuntimeException("Unable to build config with different blocks for EIP155 (" + config.eip155Block + ") and EIP158 (" + config.eip158Block + ")");
                    }
                    block = config.eip155Block;
                } else {
                    block = config.eip158Block;
                }

                if (config.chainId != null) {
                    final int chainId = config.chainId;
                    lastCandidate = Pair.of(block, new Eip160HFConfig(lastCandidate.getRight()) {
                        @Override
                        public Integer getChainId() {
                            return chainId;
                        }
                    });
                } else {
                    lastCandidate = Pair.of(block, new Eip160HFConfig(lastCandidate.getRight()));
                }
                candidates.add(lastCandidate);
            }
        }

        {
            // add candidate per each block (take last in row for same block)
            Pair<Integer, ? extends BlockchainConfig> last = candidates.remove(0);
            for (Pair<Integer, ? extends BlockchainConfig> current : candidates) {
                if (current.getLeft().compareTo(last.getLeft()) > 0) {
                    add(last.getLeft(), last.getRight());
                }
                last = current;
            }
            add(last.getLeft(), last.getRight());
        }
    }
}

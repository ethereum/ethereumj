package org.ethereum.core.genesis;

import java.util.List;

/**
 * Created by Anton on 03.03.2017.
 */
public class GenesisConfig {
    public Integer homesteadBlock;
    public Integer daoForkBlock;
    public Integer eip150Block;
    public Integer eip155Block;
    public boolean daoForkSupport;
    public Integer eip158Block;
    public Integer chainId;

    // EthereumJ private options

    public static class HashValidator {
        public long number;
        public String hash;
    }

    public List<HashValidator> headerValidators;

    public boolean isCustomConfig() {
        return homesteadBlock != null || daoForkBlock != null || eip150Block != null ||
                eip155Block != null || eip158Block != null;
    }
}

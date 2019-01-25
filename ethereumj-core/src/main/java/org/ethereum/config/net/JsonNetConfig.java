/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.config.net;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.blockchain.*;
import org.ethereum.core.genesis.GenesisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert JSON config from genesis to Java blockchain net config.
 * Created by Stan Reshetnyk on 23.12.2016.
 */
public class JsonNetConfig extends BaseNetConfig {

    private static Logger logger = LoggerFactory.getLogger("general");

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
            if (logger.isDebugEnabled())
                logger.debug("Rendering net config from genesis {} ...", config);


            Pair<Integer, ? extends BlockchainConfig> lastCandidate = Pair.of(0, initialBlockConfig);
            if (logger.isDebugEnabled())
                logger.debug("Block #{} => Frontier", lastCandidate.getLeft());
            candidates.add(lastCandidate);

            // homestead block assumed to be 0 by default
            lastCandidate = Pair.of(config.homesteadBlock == null ? 0 : config.homesteadBlock, new HomesteadConfig());
            if (logger.isDebugEnabled())
                logger.debug("Block #{} => Homestead", lastCandidate.getLeft());
            candidates.add(lastCandidate);

            if (config.daoForkBlock != null) {
                AbstractDaoConfig daoConfig = config.daoForkSupport ?
                        new DaoHFConfig(lastCandidate.getRight(), config.daoForkBlock) :
                        new DaoNoHFConfig(lastCandidate.getRight(), config.daoForkBlock);
                lastCandidate = Pair.of(config.daoForkBlock, daoConfig);
                if (logger.isDebugEnabled())
                    logger.debug("Block #{} => DaoForkSupport", lastCandidate.getLeft());
                candidates.add(lastCandidate);
            }

            if (config.eip150Block != null) {
                lastCandidate = Pair.of(config.eip150Block, new Eip150HFConfig(lastCandidate.getRight()));
                if (logger.isDebugEnabled())
                    logger.debug("Block #{} => EIP150", lastCandidate.getLeft());
                candidates.add(lastCandidate);
            }

            if (config.eip155Block != null || config.eip158Block != null) {
                int block;
                StringBuilder logLine = new StringBuilder();
                if (config.eip155Block != null) {
                    if (config.eip158Block != null && !config.eip155Block.equals(config.eip158Block)) {
                        throw new RuntimeException("Unable to build config with different blocks for EIP155 (" + config.eip155Block + ") and EIP158 (" + config.eip158Block + ")");
                    }
                    block = config.eip155Block;
                    if (logger.isDebugEnabled())
                        logLine.append("Block #").append(block).append(" => EIP155");
                } else {
                    block = config.eip158Block;
                    if (logger.isDebugEnabled())
                        logLine.append("Block #").append(block).append(" => EIP158");
                }

                if (config.chainId != null) {
                    final int chainId = config.chainId;
                    if (logger.isDebugEnabled())
                        logLine.append(", chainId: ").append(chainId);
                    lastCandidate = Pair.of(block, new Eip160HFConfig(lastCandidate.getRight()) {
                        @Override
                        public Integer getChainId() {
                            return chainId;
                        }

                        @Override
                        public String toString() {
                            return Eip160HFConfig.class.getSimpleName();
                        }
                    });
                } else {
                    lastCandidate = Pair.of(block, new Eip160HFConfig(lastCandidate.getRight()));
                }
                if (logger.isDebugEnabled())
                    logger.debug(logLine.toString());
                candidates.add(lastCandidate);
            }

            if (config.byzantiumBlock != null) {
                StringBuilder logLine = new StringBuilder();
                if (logger.isDebugEnabled())
                    logLine.append("Block #").append(config.byzantiumBlock).append(" => Byzantium");
                if (config.chainId != null) {
                    final int chainId = config.chainId;
                    if (logger.isDebugEnabled())
                        logLine.append(", chainId: ").append(chainId);
                    lastCandidate = Pair.of(config.byzantiumBlock, new ByzantiumConfig(lastCandidate.getRight()) {
                        @Override
                        public Integer getChainId() {
                            return chainId;
                        }

                        @Override
                        public String toString() {
                            return ByzantiumConfig.class.getSimpleName();
                        }
                    });
                } else {
                    lastCandidate = Pair.of(config.byzantiumBlock, new ByzantiumConfig(lastCandidate.getRight()));
                }
                if (logger.isDebugEnabled())
                    logger.debug(logLine.toString());
                candidates.add(lastCandidate);
            }

            if (config.constantinopleBlock != null) {
                StringBuilder logLine = new StringBuilder();
                if (logger.isDebugEnabled())
                    logLine.append("Block #").append(config.constantinopleBlock).append(" => Constantinople");
                if (config.chainId != null) {
                    final int chainId = config.chainId;
                    if (logger.isDebugEnabled())
                        logLine.append(", chainId: ").append(chainId);
                    lastCandidate = Pair.of(config.constantinopleBlock, new ConstantinopleConfig(lastCandidate.getRight()) {
                        @Override
                        public Integer getChainId() {
                            return chainId;
                        }

                        @Override
                        public String toString() {
                            return ConstantinopleConfig.class.getSimpleName();
                        }
                    });
                } else {
                    lastCandidate = Pair.of(config.constantinopleBlock, new ConstantinopleConfig(lastCandidate.getRight()));
                }
                if (logger.isDebugEnabled())
                    logger.debug(logLine.toString());
                candidates.add(lastCandidate);
            }

            if (config.petersburgBlock != null) {
                StringBuilder logLine = new StringBuilder();
                if (logger.isDebugEnabled())
                    logLine.append("Block #").append(config.petersburgBlock).append(" => Petersburg");
                if (config.chainId != null) {
                    final int chainId = config.chainId;
                    if (logger.isDebugEnabled())
                        logLine.append(", chainId: ").append(chainId);
                    lastCandidate = Pair.of(config.petersburgBlock, new PetersburgConfig(lastCandidate.getRight()) {
                        @Override
                        public Integer getChainId() {
                            return chainId;
                        }

                        @Override
                        public String toString() {
                            return PetersburgConfig.class.getSimpleName();
                        }
                    });
                } else {
                    lastCandidate = Pair.of(config.petersburgBlock, new PetersburgConfig(lastCandidate.getRight()));
                }
                if (logger.isDebugEnabled())
                    logger.debug(logLine.toString());
                candidates.add(lastCandidate);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("Finished rendering net config from genesis {}", config);

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

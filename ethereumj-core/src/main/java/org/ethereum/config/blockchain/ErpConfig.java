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
package org.ethereum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.BlockchainConfig;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.erp.ErpExecutor;
import org.ethereum.erp.ErpLoader;
import org.ethereum.erp.ErpLoader.ErpMetadata;
import org.ethereum.erp.StateChangeObject;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.ExtraDataPresenceRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Created by Dan Phifer, 2018-02-01.
 */
public class ErpConfig extends FrontierConfig {

    private final long EXTRA_DATA_AFFECTS_BLOCKS_NUMBER = 10;
    public static final Logger logger = LoggerFactory.getLogger("config");

    private BlockchainConfig parent;
    private Map<Long, ErpMetadata> erpDataByTargetBlock;
    private ErpLoader erpLoader;
    private ErpExecutor erpExecutor;

    public ErpConfig() {
        this(new HomesteadConfig(), new ErpLoader("/erps"), new ErpExecutor());
    }

    public ErpConfig(BlockchainConfig parent, ErpLoader erpLoader, ErpExecutor erpExecutor) {
        this.erpLoader = erpLoader;
        this.erpExecutor = erpExecutor;

        try {
            initErpConfig(parent);
        } catch (IOException e) {
            // TODO: not sure what to do here.
            logger.error("Failed to load the ERPConfig", e);
            throw new RuntimeException(e);
        }
    }

    void initErpConfig(BlockchainConfig parent) throws IOException {
        this.parent = parent;
        this.constants = parent.getConstants();

        // load the config block numbers
        final Collection<ErpMetadata> allErps = erpLoader.loadErpMetadata();
        this.erpDataByTargetBlock = allErps
            .stream()
            .collect(toMap(ErpMetadata::getTargetBlock, Function.identity()));

        logger.info("Found %d ERPs", allErps.size());

        // add the header validators for each known ERP
        final List<Pair<Long, BlockHeaderValidator>> headerValidators = headerValidators();
        erpDataByTargetBlock.forEach((targetBlock, erpMetadata) -> {
            BlockHeaderRule rule = new ExtraDataPresenceRule(erpMetadata.getErpMarker(), true);
            headerValidators.add(Pair.of(erpMetadata.getTargetBlock(), new BlockHeaderValidator(rule)));
        });
    }

    /**
     * Miners should include marker for initial 10 blocks. Either "dao-hard-fork" or ""
     */
    @Override
    public byte[] getExtraData(byte[] minerExtraData, long blockNumber) {
        // TODO: is EXTRA_DATA_AFFECTS_BLOCKS_NUMBER needed?
        final ErpMetadata erpMetadata = erpDataByTargetBlock.get(blockNumber);
        return erpMetadata != null
            ? erpMetadata.getErpMarker()
            : minerExtraData;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {
        hardForkTransfers(block.getNumber(), repo);
    }

    void hardForkTransfers(long blockNumber, Repository repo) {
        final ErpMetadata erpMetadata = erpDataByTargetBlock.get(blockNumber);
        if (erpMetadata != null) {
            logger.info("Found ERP %s for block %d", erpMetadata.getId(), blockNumber);
            final StateChangeObject sco;
            try {
                sco = erpLoader.loadStateChangeObject(erpMetadata);
            } catch (IOException e) {
                logger.error("Failed to load state change object for " + erpMetadata.getId(), e);
                throw new RuntimeException("Failed to load state change object for " + erpMetadata.getId(), e);
            }

            // TODO: Is this the right way to apply changes in  batch?
            final Repository track = repo.startTracking();
            try {
                erpExecutor.applyStateChanges(sco, repo);
                track.commit();
                logger.info("Successfully applied ERP '%s' to block %d", erpMetadata.getId(), blockNumber);
            }
            catch (ErpExecutor.ErpExecutionException e) {
                track.rollback();
                logger.error("Failed to apply ERP '%s' to block %d", erpMetadata.getId(), blockNumber, e);
            }
            catch (Exception e) {
                track.rollback();
                logger.error("Failed to apply ERP '%s' to block %d", erpMetadata.getId(), blockNumber, e);
                throw e;
            }
            finally {
                track.close();
            }
        }
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        return this.parent.calcDifficulty(curBlock, parent);
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        return parent.getTransactionCost(tx);
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return parent.acceptTransactionSignature(tx);
    }
}

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
package org.ethereum.validator;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockHeader;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.mine.EthashValidationHelper;
import org.ethereum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.ethereum.validator.EthashRule.Mode.fake;
import static org.ethereum.validator.EthashRule.Mode.mixed;

/**
 * Runs block header validation against Ethash dataset.
 *
 * <p>
 *     Configurable to work in several modes:
 *     <ul>
 *         <li> fake - partial checks without verification against Ethash dataset
 *         <li> strict - full check for each block
 *         <li> mixed  - run full check in random fashion (~1/3 blocks are checked) during long sync,
 *              run full check for each block during short sync
 *
 * @author Mikhail Kalinin
 * @since 19.06.2018
 */
public class EthashRule extends BlockHeaderRule {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");
    private static final Logger loggerEthash = LoggerFactory.getLogger("ethash");

    EthashValidationHelper ethashHelper;
    ProofOfWorkRule powRule = new ProofOfWorkRule();

    enum Mode {
        strict,
        mixed,
        fake;

        static Mode parse(String name, Mode defaultMode) {
            for (Mode mode : values()) {
                if (mode.name().equals(name.toLowerCase()))
                    return mode;
            }
            return defaultMode;
        }
    }

    private static final int MIX_DENOMINATOR = 3;
    private Mode mode = mixed;
    private boolean syncDone = false;
    private Random rnd = new Random();

    public EthashRule(SystemProperties systemProperties, CompositeEthereumListener listener) {
        mode = Mode.parse(systemProperties.getEthashMode(), mixed);

        if (mode != fake) {
            ethashHelper = new EthashValidationHelper(listener);
            listener.addListener(new EthereumListenerAdapter() {
                @Override
                public void onSyncDone(SyncState state) {
                    EthashRule.this.syncDone = true;
                }
            });
        }
    }

    @Override
    public ValidationResult validate(BlockHeader header) {

        if (header.isGenesis())
            return Success;

        if (ethashHelper == null)
            return powRule.validate(header);

        // mixed mode payload
        if (mode == mixed && !syncDone && rnd.nextInt(100) % MIX_DENOMINATOR > 0)
            return powRule.validate(header);

        try {
            Pair<byte[], byte[]> res = ethashHelper.ethashWorkFor(header, header.getNonce(), true);
            if (res == null) {
                loggerEthash.debug("PARTIAL {}", header.getShortDescr());
                return powRule.validate(header);
            }

            if (!FastByteComparisons.equal(res.getLeft(), header.getMixHash())) {
                return fault(String.format("#%d: mixHash doesn't match", header.getNumber()));
            }

            if (FastByteComparisons.compareTo(res.getRight(), 0, 32, header.getPowBoundary(), 0, 32) > 0) {
                return fault(String.format("#%d: proofValue > header.getPowBoundary()", header.getNumber()));
            }

            loggerEthash.debug("FULL {}", header.getShortDescr());

            return Success;
        } catch (Exception e) {
            logger.error("Failed to verify ethash work for block {}", header.getShortDescr(), e);
            return fault("Failed to verify ethash work for block " + header.getShortDescr());
        }
    }
}

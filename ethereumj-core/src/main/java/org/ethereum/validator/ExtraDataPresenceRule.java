/*
 * Copyright 2015, 2016 Ether.Camp Inc. (US)
 * This file is part of Ethereum Harmony.
 *
 * Ethereum Harmony is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ethereum Harmony is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ethereum Harmony.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ethereum.validator;

import org.ethereum.core.BlockHeader;
import org.ethereum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Stan Reshetnyk on 26.12.16.
 */
public class ExtraDataPresenceRule extends BlockHeaderRule {

    public final byte[] data;

    public final boolean required;

    public ExtraDataPresenceRule(byte[] data, boolean required) {
        this.data = data;
        this.required = required;
    }

    @Override
    public boolean validate(BlockHeader header) {
        errors.clear();

        if (required && !FastByteComparisons.equal(header.getExtraData(), data)) {
            errors.add("Block " + header.getNumber() + " extra data constraint violated. Expected:" +
                    Hex.toHexString(data) + ", got: " + Hex.toHexString(header.getExtraData()));
            return false;
        } else if (!required && FastByteComparisons.equal(header.getExtraData(), data)) {
            errors.add("Block " + header.getNumber() + " extra data constraint violated. Expected:" +
                    Hex.toHexString(data) + ", got: " + Hex.toHexString(header.getExtraData()));
            return false;
        }
        return true;
    }
}

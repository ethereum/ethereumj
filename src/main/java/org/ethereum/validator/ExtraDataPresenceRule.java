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

import org.ethereum.core.BlockHeader;
import org.ethereum.util.ByteUtil;
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
    public ValidationResult validate(BlockHeader header) {
        final byte[] extraData = header.getExtraData() != null ? header.getExtraData() : ByteUtil.EMPTY_BYTE_ARRAY;
        final boolean extraDataMatches = FastByteComparisons.equal(extraData, data);

        if (required && !extraDataMatches) {
            return fault("Block " + header.getNumber() + " is no-fork. Expected presence of: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        } else if (!required && extraDataMatches) {
            return fault("Block " + header.getNumber() + " is pro-fork. Expected no: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        }
        return Success;
    }
}

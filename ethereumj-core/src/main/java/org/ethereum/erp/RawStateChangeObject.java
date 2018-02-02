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
package org.ethereum.erp;

/**
 * This class maps directly to the SCO file format.
 */
public class RawStateChangeObject {
    public String erpId;
    public long targetBlock;
    public RawMetadata metadata;
    public RawStateChangeAction[] actions;

    public static class RawStateChangeAction {
        public String type;
        public String fromAddress;
        public String toAddress;
        public String valueInWei;
        public String code;
        public String expectedCodeHash;
    }

    public static class RawMetadata {
        public long sourceBlock;
        public String version;
    }
}



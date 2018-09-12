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
package org.ethereum.publish.event;

public class VmTraceCreated extends Event<VmTraceCreated.Data> {

    public static class Data {
        private final String txHash;
        private final String trace;

        public Data(String txHash, String trace) {
            this.txHash = txHash;
            this.trace = trace;
        }

        public String getTxHash() {
            return txHash;
        }

        public String getTrace() {
            return trace;
        }
    }

    public VmTraceCreated(String txHash, String trace) {
        super(new Data(txHash, trace));
    }
}

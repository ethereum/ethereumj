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
package org.ethereum.sharding.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.spongycastle.util.encoders.Hex;

/**
 * @author Mikhail Kalinin
 * @since 26.07.2018
 */
public class DepositContractConfig {

    private String abi;
    private byte[] bin;
    private byte[] address;

    public DepositContractConfig(String abi, byte[] bin, byte[] address) {
        this.abi = abi;
        this.bin = bin;
        this.address = address;
    }

    public static DepositContractConfig fromFile() {
        Config config = ConfigFactory.parseResources("deposit-contract.conf");

        return new DepositContractConfig(
                config.getString("beacon.deposit.contract.abi"),
                Hex.decode(config.getString("beacon.deposit.contract.bin")),
                Hex.decode(config.getString("beacon.deposit.contract.address"))
        );
    }

    public String getAbi() {
        return abi;
    }

    public byte[] getBin() {
        return bin;
    }

    public byte[] getAddress() {
        return address;
    }
}

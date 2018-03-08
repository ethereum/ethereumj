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
package org.ethereum.casper.core;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CasperFacade {

    @Autowired
    private SystemProperties systemProperties;

    private Ethereum ethereum;

    private CallTransaction.Contract casper = null;

    private String contractAddress;  // FIXME: why should we have casper addresses in two places?. It's already in SystemProperties

    private List<Transaction> initTxs;

    public CallTransaction.Contract getContract() {
        init();
        return casper;
    }

    private void init() {
        if (casper == null) {
            contractAddress = Hex.toHexString(systemProperties.getCasperAddress());
            String casperAbi = systemProperties.getCasperAbi();
            casper = new CallTransaction.Contract(casperAbi);
        }
    }

    public Object[] constCall(String func, Object... funcArgs) {
        init();
        ProgramResult r = ethereum.callConstantFunction(contractAddress,
                casper.getByName(func), funcArgs);
        return casper.getByName(func).decodeResult(r.getHReturn());
    }


    public Object[] constCall(Block block, String func, Object... funcArgs) {
        init();
        Transaction tx = CallTransaction.createCallTransaction(0, 0, 100000000000000L,
                contractAddress, 0, casper.getByName(func), funcArgs);
        ProgramResult r = ethereum.callConstantFunction(block, contractAddress,
                casper.getByName(func), funcArgs);
        return casper.getByName(func).decodeResult(r.getHReturn());
    }

    public byte[] getAddress() {
        return Hex.decode(contractAddress);
    }

    public List<Transaction> getInitTxs() {
        return initTxs;
    }

    public void setInitTxs(List<Transaction> initTxs) {
        this.initTxs = initTxs;
    }

    public void setEthereum(Ethereum ethereum) {
        this.ethereum = ethereum;
    }
}

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
package org.ethereum.json;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.core.Repository;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.DataWord;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON Helper class to format data into ObjectNodes
 * to match PyEthereum blockstate output
 *
 *  Dump format:
 *  {
 *      "address":
 *      {
 *          "nonce": "n1",
 *          "balance": "b1",
 *          "stateRoot": "s1",
 *          "codeHash": "c1",
 *          "code": "c2",
 *          "storage":
 *          {
 *              "key1": "value1",
 *              "key2": "value2"
 *          }
 *      }
 *  }
 *
 * @author Roman Mandeleil
 * @since 26.06.2014
 */
public class JSONHelper {

    @SuppressWarnings("uncheked")
    public static void dumpState(ObjectNode statesNode, String address, AccountState state, ContractDetails details) {

        List<DataWord> storageKeys = new ArrayList<>(details.getStorage().keySet());
        Collections.sort(storageKeys);

        ObjectNode account = statesNode.objectNode();
        ObjectNode storage = statesNode.objectNode();

        for (DataWord key : storageKeys) {
            storage.put("0x" + Hex.toHexString(key.getData()),
                    "0x" + Hex.toHexString(details.getStorage().get(key).getNoLeadZeroesData()));
        }

        if (state == null)
            state = new AccountState(SystemProperties.getDefault().getBlockchainConfig().getCommonConstants().getInitialNonce(),
                    BigInteger.ZERO);

        account.put("balance", state.getBalance() == null ? "0" : state.getBalance().toString());
//        account.put("codeHash", details.getCodeHash() == null ? "0x" : "0x" + Hex.toHexString(details.getCodeHash()));
        account.put("code", details.getCode() == null ? "0x" : "0x" + Hex.toHexString(details.getCode()));
        account.put("nonce", state.getNonce() == null ? "0" : state.getNonce().toString());
        account.set("storage", storage);
        account.put("storage_root", state.getStateRoot() == null ? "" : Hex.toHexString(state.getStateRoot()));

        statesNode.set(address, account);
    }

    public static void dumpBlock(ObjectNode blockNode, Block block,
                                 long gasUsed, byte[] state, List<ByteArrayWrapper> keys,
                                 Repository repository) {

        blockNode.put("coinbase", Hex.toHexString(block.getCoinbase()));
        blockNode.put("difficulty", new BigInteger(1, block.getDifficulty()).toString());
        blockNode.put("extra_data", "0x");
        blockNode.put("gas_used", String.valueOf(gasUsed));
        blockNode.put("nonce", "0x" + Hex.toHexString(block.getNonce()));
        blockNode.put("number", String.valueOf(block.getNumber()));
        blockNode.put("prevhash", "0x" + Hex.toHexString(block.getParentHash()));

        ObjectNode statesNode = blockNode.objectNode();
        for (ByteArrayWrapper key : keys) {
            byte[] keyBytes = key.getData();
            AccountState accountState = repository.getAccountState(keyBytes);
            ContractDetails details = repository.getContractDetails(keyBytes);
            dumpState(statesNode, Hex.toHexString(keyBytes), accountState, details);
        }
        blockNode.set("state", statesNode);

        blockNode.put("state_root", Hex.toHexString(state));
        blockNode.put("timestamp", String.valueOf(block.getTimestamp()));

        ArrayNode transactionsNode = blockNode.arrayNode();
        blockNode.set("transactions", transactionsNode);

        blockNode.put("tx_list_root", ByteUtil.toHexString(block.getTxTrieRoot()));
        blockNode.put("uncles_hash", "0x" + Hex.toHexString(block.getUnclesHash()));

//      JSONHelper.dumpTransactions(blockNode,
//              stateRoot, codeHash, code, storage);
    }

}

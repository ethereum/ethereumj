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
package org.ethereum.samples.util;

import org.ethereum.crypto.ECKey;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.ethereum.crypto.HashUtil.sha3;

public class Account {

    public static class Register {

        private static final String FAUCET_NAME = "faucet";

        private final Map<String, Account> map = new HashMap<>();

        public Register add(String name, String password) {
            map.put(name, new Account(password));
            return this;
        }

        public Register addSameNameAndPass(String name) {
            return add(name, name);
        }

        public Account get(String name) {
            Account value = map.get(name);
            if (value == null) {
                throw new RuntimeException("Account with name " + name + " isn't registered.");
            }
            return value;
        }

        public Set<Account> accounts(Predicate<String> filter) {
            return map.entrySet().stream()
                    .filter(e -> isNull(filter) || filter.test(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
        }

        public Set<Account> accounts() {
            return accounts(null);
        }

        public Register withFaucet(String password) {
            return add(FAUCET_NAME, password);
        }

        public Account getFaucet() {
            return get(FAUCET_NAME);
        }

        public Set<Account> accountsWithoutFaucet() {
            return accounts(name -> !FAUCET_NAME.equals(name));
        }
    }

    private final ECKey key;
    private BigInteger requiredBalance;

    public Account(String password) {
        this.key = ECKey.fromPrivate(sha3(password.getBytes()));
    }

    public ECKey getKey() {
        return key;
    }

    public byte[] getAddress() {
        return key.getAddress();
    }

    public static Register newRegister() {
        return new Register();
    }
}
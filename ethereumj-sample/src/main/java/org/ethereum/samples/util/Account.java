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
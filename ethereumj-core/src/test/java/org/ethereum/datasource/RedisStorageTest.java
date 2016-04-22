package org.ethereum.datasource;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RedisStorageTest extends AbstractRedisTest {

    @Test
    public void testRedisSet() {
        if (!isConnected()) return;

        Pojo elephant = Pojo.create(5L, "elephant");
        Pojo lion = Pojo.create(5L, "lion");

        Set<Pojo> ranch = getRedisConnection().createSetFor(Pojo.class, "ranch");
        Pojo chicken = Pojo.create(1L, "chicken");
        Pojo cow = Pojo.create(2L, "cow");
        Pojo puppy = Pojo.create(3L, "puppy");
        Pojo kitten = Pojo.create(4L, "kitten");

        assertTrue(ranch.add(chicken));
        assertFalse(ranch.add(chicken));
        assertTrue(ranch.contains(chicken));
        assertEquals(1, ranch.size());

        Pojo next = ranch.iterator().next();
        assertNotNull(next);
        assertEquals(chicken, next);

        assertTrue(ranch.addAll(asList(cow, puppy, kitten)));
        assertEquals(4, ranch.size());
        assertFalse(ranch.isEmpty());
        assertFalse(ranch.remove(elephant));
        assertFalse(ranch.removeAll(asList(cow, lion, elephant)));
        assertEquals(3, ranch.size());

        assertTrue(ranch.retainAll(asList(kitten, puppy)));
        assertEquals(2, ranch.size());

        ranch.clear();
        assertEquals(0, ranch.size());
        assertTrue(ranch.isEmpty());
    }

    @Test
    public void testSeveralSetsWithOneName() {
        if (!isConnected()) return;

        final String name = "testTransactions";
        Set<Transaction> transactions = getRedisConnection().createTransactionSet(name);
        transactions.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "cat"));
        transactions.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "dog"));
        transactions.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "rabbit"));

        Set<Transaction> transactions1 = getRedisConnection().createTransactionSet(name);
        transactions1.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "duck"));
        transactions1.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "chicken"));
        transactions1.add(createTransaction("09184e72a000", "4255", "1000000000000000000000", "cow"));

        assertEquals(6, transactions1.size());
        transactions.clear();
        assertTrue(transactions1.isEmpty());
    }

    private static class Pojo {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static Pojo create(long id, String name) {
            Pojo result = new Pojo();
            result.setId(id);
            result.setName(name);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !getClass().isInstance(obj)) return false;
            if (this == obj) return true;

            Pojo another = (Pojo) obj;
            return (Objects.equals(another.getId(), getId())) && another.getName().equals(getName());
        }

        @Override
        public int hashCode() {
            int hashCode = 17;

            hashCode += ((getId() == null) ? 0 : getId().hashCode()) * 31;
            hashCode += ((getName() == null) ? 0 : getName().hashCode()) * 31;

            return hashCode;
        }
    }

    public static Transaction createTransaction(String gasPrice, String gas, String val, String secret) {

        ECKey ecKey = ECKey.fromPrivate(HashUtil.sha3(secret.getBytes()));

        // Tn (nonce); Tp(pgas); Tg(gaslimi); Tt(value); Tv(value); Ti(sender);  Tw; Tr; Ts
        return new Transaction(null, Hex.decode(gasPrice), Hex.decode(gas), ecKey.getAddress(),
                new BigInteger(val).toByteArray(),
                null);
    }
}
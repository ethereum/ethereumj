package org.ethereum.datasource.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.ContractDetails;

import java.util.HashSet;
import java.util.Set;

public final class Serializers {

    private static abstract class BaseRedisSerializer<T> implements RedisSerializer<T> {

        public abstract boolean supports(Class<?> aClass);

        @Override
        public boolean canSerialize(Object o) {
            return (o != null) && supports(o.getClass());
        }
    }

    private static class JacksonJsonRedisSerializer<T> implements RedisSerializer<T> {

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final JavaType javaType;

        JacksonJsonRedisSerializer(Class<T> clazz) {
            this.javaType = TypeFactory.defaultInstance().constructType(clazz);
        }

        @Override
        public T deserialize(byte[] bytes) {
            if (isEmpty(bytes)) return null;

            try {
                return (T) this.objectMapper.readValue(bytes, 0, bytes.length, javaType);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot read JSON: " + ex.getMessage(), ex);
            }
        }

        @Override
        public byte[] serialize(Object t) {
            if (t == null) return EMPTY_ARRAY;

            try {
                return this.objectMapper.writeValueAsBytes(t);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot write JSON: " + ex.getMessage(), ex);
            }
        }

        @Override
        public boolean canSerialize(Object o) {
            return (o != null) && javaType.hasRawClass(o.getClass());
        }
    }

    private static class TransactionReceiptSerializer extends BaseRedisSerializer<TransactionReceipt> {

        @Override
        public boolean supports(Class<?> aClass) {
            return TransactionReceipt.class.isAssignableFrom(aClass);
        }


        @Override
        public byte[] serialize(TransactionReceipt transactionReceipt) {
            return (transactionReceipt == null) ? EMPTY_ARRAY : transactionReceipt.getEncoded();
        }

        @Override
        public TransactionReceipt deserialize(byte[] bytes) {
            return isEmpty(bytes) ? null : new TransactionReceipt(bytes);
        }
    }

    private static class TransactionSerializer extends BaseRedisSerializer<Transaction> {

        @Override
        public boolean supports(Class<?> aClass) {
            return Transaction.class.isAssignableFrom(aClass);
        }

        @Override
        public byte[] serialize(Transaction transaction) {
            return (transaction == null) ? EMPTY_ARRAY : transaction.getEncoded();
        }

        @Override
        public Transaction deserialize(byte[] bytes) {
            return isEmpty(bytes) ? null : new Transaction(bytes);
        }
    }

    private static class AccountStateSerializer extends BaseRedisSerializer<AccountState> {

        @Override
        public boolean supports(Class<?> aClass) {
            return AccountState.class.isAssignableFrom(aClass);
        }

        @Override
        public byte[] serialize(AccountState accountState) {
            return (accountState == null) ? EMPTY_ARRAY : accountState.getEncoded();
        }

        @Override
        public AccountState deserialize(byte[] bytes) {
            return isEmpty(bytes) ? null : new AccountState(bytes);
        }
    }

    private static class BlockSerializer extends BaseRedisSerializer<Block> {

        @Override
        public boolean supports(Class<?> aClass) {
            return Block.class.isAssignableFrom(aClass);
        }

        @Override
        public byte[] serialize(Block block) {
            return (block == null) ? EMPTY_ARRAY : block.getEncoded();
        }

        @Override
        public Block deserialize(byte[] bytes) {
            return isEmpty(bytes) ? null : new Block(bytes);
        }
    }

    private static class ContractDetailsSerializer extends BaseRedisSerializer<ContractDetails> {

        @Override
        public boolean supports(Class<?> aClass) {
            return ContractDetails.class.isAssignableFrom(aClass);
        }

        @Override
        public byte[] serialize(ContractDetails contractDetails) {
            return (contractDetails == null) ? EMPTY_ARRAY : contractDetails.getEncoded();
        }

        @Override
        public ContractDetails deserialize(byte[] bytes) {
            return isEmpty(bytes) ? null : new ContractDetails(bytes);
        }
    }


    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final Set<? extends BaseRedisSerializer> SERIALIZERS = new HashSet<BaseRedisSerializer>() {{
        add(new TransactionSerializer());
        add(new TransactionReceiptSerializer());
        add(new AccountStateSerializer());
        add(new BlockSerializer());
        add(new ContractDetailsSerializer());
    }};

    public static <T> RedisSerializer<T> forClass(Class<T> clazz) {
        for (BaseRedisSerializer serializer : SERIALIZERS) {
            if (serializer.supports(clazz)) {
                return serializer;
            }
        }
        return new JacksonJsonRedisSerializer(clazz);
    }

    private static boolean isEmpty(byte[] data) {
        return (data == null || data.length == 0);
    }
}

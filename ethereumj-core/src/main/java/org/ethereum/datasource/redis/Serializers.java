package org.ethereum.datasource.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;

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
            return transactionReceipt.getEncoded();
        }

        @Override
        public TransactionReceipt deserialize(byte[] bytes) {
            return new TransactionReceipt(bytes);
        }
    }

    private static class TransactionSerializer extends BaseRedisSerializer<Transaction> {

        @Override
        public boolean supports(Class<?> aClass) {
            return Transaction.class.isAssignableFrom(aClass);
        }

        @Override
        public byte[] serialize(Transaction transaction) {
            return transaction.getEncoded();
        }

        @Override
        public Transaction deserialize(byte[] bytes) {
            return new Transaction(bytes);
        }
    }

    
    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final Set<? extends BaseRedisSerializer> SERIALIZERS = new HashSet<BaseRedisSerializer>() {{
        add(new TransactionSerializer());
        add(new TransactionReceiptSerializer());
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

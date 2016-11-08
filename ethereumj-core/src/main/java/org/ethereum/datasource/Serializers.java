package org.ethereum.datasource;

import org.ethereum.core.AccountState;
import org.ethereum.util.RLP;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;

/**
 * Created by Anton Nashatyrev on 08.11.2016.
 */
public class Serializers {
    public final static Serializer<AccountState, byte[]> AccountStateSerializer = new Serializer<AccountState, byte[]>() {
        @Override
        public byte[] serialize(AccountState object) {
            return object.getEncoded();
        }

        @Override
        public AccountState deserialize(byte[] stream) {
            return stream == null || stream.length == 0 ? null : new AccountState(stream);
        }
    };

    public final static Serializer<DataWord, byte[]> WordSerializer = new Serializer<DataWord, byte[]>() {
        @Override
        public byte[] serialize(DataWord object) {
            return object.getData();
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            return new DataWord(stream);
        }
    };

    public final static Serializer<DataWord, byte[]> TrieWordSerializer = new Serializer<DataWord, byte[]>() {
        @Override
        public byte[] serialize(DataWord object) {
            return RLP.encodeElement(object.getNoLeadZeroesData());
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            if (stream == null || stream.length == 0) return null;
            byte[] dataDecoded = RLP.decode2(stream).get(0).getRLPData();
            return new DataWord(dataDecoded);
        }
    };

    public final static Serializer<Value, byte[]> TrieCacheSerializer = new Serializer<Value, byte[]>() {
        @Override
        public byte[] serialize(Value object) {
            return object.encode();
        }

        @Override
        public Value deserialize(byte[] stream) {
            return Value.fromRlpEncoded(stream);
        }
    };
}

package org.ethereum.net.swarm;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Admin on 11.06.2015.
 */
public class SimpleDPA extends DPA {
    Random rnd = new Random(0);
    Map<Key, SectionReader> store = new HashMap<>();

    public SimpleDPA() {
        super(null);
    }

    @Override
    public SectionReader retrieve(Key key) {
        return store.get(key);
    }

    @Override
    public Key store(SectionReader reader) {
        byte[] bb = new byte[16];
        rnd.nextBytes(bb);
        Key key = new Key(bb);
        store.put(key, reader);
        return key;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SimpleDPA:\n");
        for (Map.Entry<Key, SectionReader> entry : store.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
                    .append(Util.readerToString(entry.getValue())).append('\n');
        }
        return sb.toString();
    }


}

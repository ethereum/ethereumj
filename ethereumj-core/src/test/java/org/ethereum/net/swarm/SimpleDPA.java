package org.ethereum.net.swarm;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Admin on 11.06.2015.
 */
public class SimpleDPA extends DPATmp {
    Map<String, ByteBuf> store = new HashMap<>();

    @Override
    public ByteBuf read(String hash) {
        return store.get(hash);
    }

    @Override
    public String store(ByteBuf data) {
        String uuid = UUID.randomUUID().toString().substring(0,8);
        store.put(uuid, data);
        return uuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SimpleDPA:\n");
        for (Map.Entry<String, ByteBuf> entry : store.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
                    .append(entry.getValue().toString(StandardCharsets.UTF_8)).append('\n');
        }
        return sb.toString();
    }


}

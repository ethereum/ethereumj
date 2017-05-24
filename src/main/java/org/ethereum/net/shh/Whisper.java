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
package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

/**
 * Created by Anton Nashatyrev on 25.09.2015.
 */
@Component
public abstract class Whisper {

    public abstract String addIdentity(ECKey key);

    public abstract String newIdentity();

    public abstract void watch(MessageWatcher f);

    public abstract void unwatch(MessageWatcher f);

    public void send(byte[] payload, Topic[] topics) {
        send(null, null, payload, topics, 50, 50);
    }
    public void send(byte[] payload, Topic[] topics, int ttl, int workToProve) {
        send(null, null, payload, topics, ttl, workToProve);
    }
    public void send(String toIdentity, byte[] payload, Topic[] topics) {
        send(null, toIdentity, payload, topics, 50, 50);
    }
    public void send(String toIdentity, byte[] payload, Topic[] topics, int ttl, int workToProve) {
        send(null, toIdentity, payload, topics, ttl, workToProve);
    }

    public void send(String fromIdentity, String toIdentity, byte[] payload, Topic[] topics) {
        send(fromIdentity, toIdentity, payload, topics, 50, 50);
    }

    public abstract void send(String fromIdentity, String toIdentity, byte[] payload, Topic[] topics, int ttl, int workToProve);
}

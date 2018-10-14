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
package org.ethereum.sharding.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.io.ByteStreams;
import org.ethereum.sharding.processing.state.AttestationRecord;
import org.ethereum.util.Utils;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.ethereum.util.Utils.parseHex;

/**
 * A special beacon chain block that is the first block of the chain.
 *
 * @author Mikhail Kalinin
 * @since 14.08.2018
 */
public class BeaconGenesis extends Beacon {

    public static final long SLOT = 0;

    private static final byte[] EMPTY = new byte[32];

    private static BeaconGenesis instance;

    /* Genesis timestamp, a source of slot's time frames */
    private long timestamp = 0L;
    /* A list of public keys that identifies participants of initial active validator set */
    private List<String> initialValidators = new ArrayList<>();

    BeaconGenesis(Json json) {
        super(json.parentHash(), json.randaoReveal(), json.mainChainRef(), EMPTY, SLOT, new ArrayList<>());
        this.timestamp = json.timestamp();
        this.initialValidators = json.validatorSet();
    }

    public static BeaconGenesis instance() {
        if (instance == null) {
            Json json = Json.fromResource("beacon-genesis.json");
            instance = new BeaconGenesis(json);
        }
        return instance;
    }

    public BigInteger getScore() {
        return BigInteger.ZERO;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getInitialValidators() {
        return initialValidators;
    }

    @Override
    public String toString() {
        return super.toString() + ", timestamp: " + timestamp;
    }

    @JsonSerialize
    static class Json {
        String parentHash;
        String randaoReveal;
        String mainChainRef;
        long timestamp;
        String[] validatorSet;

        byte[] parentHash() {
            return parseHex(parentHash);
        }

        byte[] randaoReveal() {
            return parseHex(randaoReveal);
        }

        byte[] mainChainRef() {
            return parseHex(mainChainRef);
        }

        long timestamp() {
            return timestamp * 1000; // seconds to millis
        }

        List<String> validatorSet() {
            List<String> validators = new ArrayList<>(validatorSet.length);
            for (String val : validatorSet) {
                if ("0x".equals(val.substring(0, 2))) {
                    validators.add(val.substring(2));
                } else {
                    validators.add(val);
                }
            }
            return validators;
        }

        static Json fromResource(String resourceName) {
            return fromStream(Json.class.getClassLoader().getResourceAsStream(resourceName));
        }

        static Json fromStream(InputStream in) {
            String json = null;
            try {
                json = new String(ByteStreams.toByteArray(in));
                ObjectMapper mapper = new ObjectMapper()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

                return mapper.readValue(json, Json.class);
            } catch (Exception e) {
                Utils.showErrorAndExit("Failed to parse beacon chain genesis: " + e.getMessage(), json);
                throw new RuntimeException(e);
            }
        }
    }
}

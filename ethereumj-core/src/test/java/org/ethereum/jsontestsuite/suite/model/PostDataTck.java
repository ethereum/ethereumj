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
package org.ethereum.jsontestsuite.suite.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.ethereum.jsontestsuite.suite.Logs;

import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 09.08.2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDataTck {

    String hash;
    @JsonDeserialize(using = Logs.Deserializer.class)
    Logs logs;
    Indexes indexes;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Logs getLogs() {
        return logs;
    }

    public void setLogs(Logs logs) {
        this.logs = logs;
    }

    public Indexes getIndexes() {
        return indexes;
    }

    public void setIndexes(Indexes indexes) {
        this.indexes = indexes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Indexes {
        Integer data;
        Integer gas;
        Integer value;

        public Integer getData() {
            return data;
        }

        public void setData(Integer data) {
            this.data = data;
        }

        public Integer getGas() {
            return gas;
        }

        public void setGas(Integer gas) {
            this.gas = gas;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}

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
package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.ethereum.core.BlockHeader;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import static org.spongycastle.util.encoders.Hex.decode;

/**
 * @author Mikhail Kalinin
 * @since 03.09.2015
 */
public class EthashTestCase {

    @JsonIgnore
    private String name;

    private String nonce;

    private String mixHash;
    private String header;
    private String seed;
    private String result;

    @JsonProperty("cache_size")
    private String cacheSize;

    @JsonProperty("full_size")
    private String fullSize;

    @JsonProperty("header_hash")
    private String headerHash;

    @JsonProperty("cache_hash")
    private String cacheHash;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getMixHash() {
        return mixHash;
    }

    public void setMixHash(String mixHash) {
        this.mixHash = mixHash;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getFullSize() {
        return fullSize;
    }

    public void setFullSize(String fullSize) {
        this.fullSize = fullSize;
    }

    public String getHeaderHash() {
        return headerHash;
    }

    public void setHeaderHash(String headerHash) {
        this.headerHash = headerHash;
    }

    public String getCacheHash() {
        return cacheHash;
    }

    public void setCacheHash(String cacheHash) {
        this.cacheHash = cacheHash;
    }

    public BlockHeader getBlockHeader() {
        RLPList rlp = RLP.decode2(decode(header));
        return new BlockHeader((RLPList) rlp.get(0));
    }

    public byte[] getResultBytes() {
        return decode(result);
    }

    @Override
    public String toString() {
        return "EthashTestCase{" +
                "name='" + name + '\'' +
                ", header='" + header + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}

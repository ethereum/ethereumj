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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ethereum.config.BlockchainNetConfig;
import org.ethereum.jsontestsuite.GitHubJSONTestSuite;
import org.ethereum.jsontestsuite.suite.model.AccountTck;
import org.ethereum.jsontestsuite.suite.model.BlockHeaderTck;
import org.ethereum.jsontestsuite.suite.model.BlockTck;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockTestCase {

    private String acomment;
    private List<BlockTck> blocks;
    private BlockHeaderTck genesisBlockHeader;
    private String genesisRLP;
    private Map<String, AccountTck> pre;
    private Map<String, AccountTck> postState;
    private String lastblockhash;
    private int noBlockChainHistory;
    private GitHubJSONTestSuite.Network network;
    private SealEngine sealEngine = SealEngine.Ethash;

    public BlockTestCase() {
    }

    public String getLastblockhash() {
        return lastblockhash;
    }

    public void setLastblockhash(String lastblockhash) {
        this.lastblockhash = lastblockhash;
    }

    public List<BlockTck> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockTck> blocks) {
        this.blocks = blocks;
    }

    public BlockHeaderTck getGenesisBlockHeader() {
        return genesisBlockHeader;
    }

    public void setGenesisBlockHeader(BlockHeaderTck genesisBlockHeader) {
        this.genesisBlockHeader = genesisBlockHeader;
    }

    public Map<String, AccountTck> getPre() {
        return pre;
    }

    public String getGenesisRLP() {
        return genesisRLP;
    }

    public void setPre(Map<String, AccountTck> pre) {
        this.pre = pre;
    }

    public Map<String, AccountTck> getPostState() {
        return postState;
    }

    public void setPostState(Map<String, AccountTck> postState) {
        this.postState = postState;
    }

    public int getNoBlockChainHistory() {
        return noBlockChainHistory;
    }

    public void setNoBlockChainHistory(int noBlockChainHistory) {
        this.noBlockChainHistory = noBlockChainHistory;
    }

    public String getAcomment() {
        return acomment;
    }

    public void setAcomment(String acomment) {
        this.acomment = acomment;
    }

    public GitHubJSONTestSuite.Network getNetwork() {
        return network;
    }

    public void setNetwork(GitHubJSONTestSuite.Network network) {
        this.network = network;
    }

    public BlockchainNetConfig getConfig() {
        return network.getConfig();
    }

    public SealEngine getSealEngine() {
        return sealEngine;
    }

    public void setSealEngine(SealEngine sealEngine) {
        this.sealEngine = sealEngine;
    }

    @Override
    public String toString() {
        return "BlockTestCase{" +
                "blocks=" + blocks +
                ", genesisBlockHeader=" + genesisBlockHeader +
                ", pre=" + pre +
                '}';
    }

    enum SealEngine {
        NoProof,  // blocks in the test should not be checked for difficulty
        Ethash
    }
}

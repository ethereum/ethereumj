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
package org.ethereum.solidity.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompilationResult {

    @JsonProperty("contracts") private Map<String, ContractMetadata> contracts;
    @JsonProperty("version") public String version;

    @JsonIgnore public static CompilationResult parse(String rawJson) throws IOException {
        if(rawJson == null || rawJson.isEmpty()){
            CompilationResult empty = new CompilationResult();
            empty.contracts = Collections.emptyMap();
            empty.version = "";

            return empty;
        } else {
            return new ObjectMapper().readValue(rawJson, CompilationResult.class);
        }
    }

    @JsonIgnore public Path getContractPath() {
        if (contracts.size() > 1) {
            throw new UnsupportedOperationException("Source contains more than 1 contact. Please specify the contract name. Available keys (" + getContractKeys().values() + ").");
        } else {
            String key = contracts.keySet().iterator().next();
            return Paths.get(key.substring(0, key.lastIndexOf(':')));
        }
    }

    @JsonIgnore public String getContractName() {
        if (contracts.size() > 1) {
            throw new UnsupportedOperationException("Source contains more than 1 contact. Please specify the contract name. Available keys (" + getContractKeys().values() + ").");
        } else {
            String key = contracts.keySet().iterator().next();
            return key.substring(key.lastIndexOf(':') + 1);
        }
    }

    @JsonIgnore public Map<Path, String> getContractKeys() {
        return contracts.keySet().stream().collect(Collectors.toMap(
                key -> Paths.get(key.substring(0, key.lastIndexOf(':'))),
                key -> key.substring(key.lastIndexOf(':') + 1)
        ));
    }

    @JsonIgnore public ContractMetadata getContract(String contractName) {
        if (contractName == null && contracts.size() == 1) {
            return contracts.values().iterator().next();
        } else if (contractName == null || contractName.isEmpty()) {
            throw new UnsupportedOperationException("Source contains more than 1 contact. Please specify the contract name. Available keys (" + getContractKeys().values() + ").");
        }
        for (Map.Entry<String, ContractMetadata> entry : contracts.entrySet()) {
            String key = entry.getKey();
            String name = key.substring(key.lastIndexOf(':') + 1);
            if (contractName.equals(name)) {
                return entry.getValue();
            }
        }
        throw new UnsupportedOperationException("Source contains more than 1 contact. Please specify a valid contract name. Available keys (" + getContractKeys().values() + ").");
    }

    @JsonIgnore public ContractMetadata getContract(Path contractPath, String contractName) {
        return contracts.get(contractPath.toAbsolutePath().toString() + ':' + contractName);
    }

    @JsonIgnore public Collection<ContractMetadata> getContracts() {
        return contracts.values();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractMetadata {
        public String abi;
        public String bin;
        public String solInterface;
        public String metadata;

        public String getInterface() {
            return solInterface;
        }

        public void setInterface(String solInterface) {
            this.solInterface = solInterface;
        }
    }
}

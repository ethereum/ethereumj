package org.ethereum.solidity.compiler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompilationResult {

    public Map<String, ContractMetadata> contracts;
    public String version;

    public static CompilationResult parse(String rawJson) throws IOException {
        if(rawJson == null || rawJson.isEmpty()){
            CompilationResult empty = new CompilationResult();
            empty.contracts = Collections.emptyMap();
            empty.version = "";

            return empty;
        } else {
            return new ObjectMapper().readValue(rawJson, CompilationResult.class);
        }
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

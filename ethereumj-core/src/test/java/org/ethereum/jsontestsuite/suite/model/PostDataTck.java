package org.ethereum.jsontestsuite.suite.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 09.08.2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDataTck {

    String hash;
    List<LogTck> logs;
    Indexes indexes;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<LogTck> getLogs() {
        return logs;
    }

    public void setLogs(List<LogTck> logs) {
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

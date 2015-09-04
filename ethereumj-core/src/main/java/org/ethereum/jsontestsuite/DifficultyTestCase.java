package org.ethereum.jsontestsuite;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ethereum.core.BlockHeader;

import java.math.BigInteger;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyTestCase {

    private static final byte[] EMPTY_ARRAY = new byte[0];

    @JsonIgnore
    private String name;

    // Test data
    private String parentTimestamp;
    private String parentDifficulty;
    private String currentTimestamp;
    private String currentBlockNumber;
    private String currentDifficulty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentTimestamp() {
        return parentTimestamp;
    }

    public void setParentTimestamp(String parentTimestamp) {
        this.parentTimestamp = parentTimestamp;
    }

    public String getParentDifficulty() {
        return parentDifficulty;
    }

    public void setParentDifficulty(String parentDifficulty) {
        this.parentDifficulty = parentDifficulty;
    }

    public String getCurrentTimestamp() {
        return currentTimestamp;
    }

    public void setCurrentTimestamp(String currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    public String getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public void setCurrentBlockNumber(String currentBlockNumber) {
        this.currentBlockNumber = currentBlockNumber;
    }

    public String getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }

    public BlockHeader getCurrent() {
        return new BlockHeader(
                EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                Long.valueOf(currentBlockNumber), 0, 0,
                Long.valueOf(currentTimestamp),
                EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY
        );
    }

    public BlockHeader getParent() {
        return new BlockHeader(
                EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY,
                new BigInteger(parentDifficulty).toByteArray(),
                Long.valueOf(currentBlockNumber) - 1, 0, 0,
                Long.valueOf(parentTimestamp),
                EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY
        );
    }

    public BigInteger getExpectedDifficulty() {
        return new BigInteger(currentDifficulty);
    }

    @Override
    public String toString() {
        return "DifficultyTestCase{" +
                "name='" + name + '\'' +
                ", parentTimestamp='" + parentTimestamp + '\'' +
                ", parentDifficulty='" + parentDifficulty + '\'' +
                ", currentTimestamp='" + currentTimestamp + '\'' +
                ", currentBlockNumber='" + currentBlockNumber + '\'' +
                ", currentDifficulty='" + currentDifficulty + '\'' +
                '}';
    }
}

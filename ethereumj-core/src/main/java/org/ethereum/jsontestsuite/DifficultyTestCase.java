package org.ethereum.jsontestsuite;

/**
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyTestCase {

    private String parentTimestamp;
    private String parentDifficulty;
    private String currentTimestamp;
    private String currentBlockNumber;
    private String currentDifficulty;

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

    @Override
    public String toString() {
        return "DifficultyTestCase{" +
                "parentTimestamp='" + parentTimestamp + '\'' +
                ", parentDifficulty='" + parentDifficulty + '\'' +
                ", currentTimestamp='" + currentTimestamp + '\'' +
                ", currentBlockNumber='" + currentBlockNumber + '\'' +
                ", currentDifficulty='" + currentDifficulty + '\'' +
                '}';
    }
}

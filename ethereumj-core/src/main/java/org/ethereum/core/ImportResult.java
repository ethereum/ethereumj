package org.ethereum.core;

public enum ImportResult {
    IMPORTED_BEST,
    IMPORTED_NOT_BEST,
    EXIST,
    NO_PARENT,
    CONSENSUS_BREAK;

    public boolean isSuccessful() {
        return equals(IMPORTED_BEST) || equals(IMPORTED_NOT_BEST);
    }
}

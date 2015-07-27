package org.ethereum.core;

public enum ImportResult {
    IMPORTED_BEST,
    IMPORTED_NOT_BEST,
    EXIST,
    NO_PARENT,
    CONSENSUS_BREAK
}

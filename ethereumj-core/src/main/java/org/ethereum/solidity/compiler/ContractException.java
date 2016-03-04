package org.ethereum.solidity.compiler;

public class ContractException extends RuntimeException {

    public ContractException(String message) {
        super(message);
    }

    public static ContractException permissionError(String msg, Object... args) {
        return error("contract permission error", msg, args);
    }

    public static ContractException compilationError(String msg, Object... args) {
        return error("contract compilation error", msg, args);
    }

    public static ContractException validationError(String msg, Object... args) {
        return error("contract validation error", msg, args);
    }

    public static ContractException assembleError(String msg, Object... args) {
        return error("contract assemble error", msg, args);
    }

    private static ContractException error(String title, String message, Object... args) {
        return new ContractException(title + ": " + String.format(message, args));
    }
}

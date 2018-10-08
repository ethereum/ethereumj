package org.ethereum.exception;

/**
 * Exception that should be thrown in extraordinary cases when Ethereum instance can't support consistent state.
 *
 * @author Eugene Shevchenko
 */
public class FatalEthereumException extends EthereumException {

    public FatalEthereumException() {
        super();
    }

    public FatalEthereumException(String message, Object... args) {
        super(String.format(message, args));
    }

    public FatalEthereumException(String message, Throwable cause) {
        super(message, cause);
    }
}

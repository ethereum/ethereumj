package org.ethereum.exception;

/**
 * Base Ethereum exception class.
 *
 * @author Eugene Shevchenko
 */
public class EthereumException extends RuntimeException{

    public EthereumException() {
    }

    public EthereumException(String message) {
        super(message);
    }

    public EthereumException(String message, Throwable cause) {
        super(message, cause);
    }

    public EthereumException(Throwable cause) {
        super(cause);
    }

    public EthereumException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

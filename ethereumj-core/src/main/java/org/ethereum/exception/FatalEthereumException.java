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

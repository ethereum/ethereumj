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
package org.ethereum.datasource;

import java.util.Set;

/**
 * Interface represents DB source which is normally the final Source in the chain
 */
public interface DbSource<V> extends BatchSource<byte[], V> {

    /**
     * Sets the DB name.
     * This could be the underlying DB table/dir name
     */
    void setName(String name);

    /**
     * @return DB name
     */
    String getName();

    /**
     * Initializes DB (open table, connection, etc)
     */
    void init();

    /**
     * @return true if DB connection is alive
     */
    boolean isAlive();

    /**
     * Closes the DB table/connection
     */
    void close();

    /**
     * @return DB keys if this option is available
     * @throws RuntimeException if the method is not supported
     */
    Set<byte[]> keys() throws RuntimeException;
}

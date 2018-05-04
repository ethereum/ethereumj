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

/**
 * Defines configurable database settings
 *
 * @author Mikhail Kalinin
 * @since 26.04.2018
 */
public class DbSettings {

    public static final DbSettings DEFAULT = new DbSettings()
            .withMaxThreads(1)
            .withMaxOpenFiles(32);

    int maxOpenFiles;
    int maxThreads;

    private DbSettings() {
    }

    public static DbSettings newInstance() {
        DbSettings settings = new DbSettings();
        settings.maxOpenFiles = DEFAULT.maxOpenFiles;
        settings.maxThreads = DEFAULT.maxThreads;
        return settings;
    }

    public int getMaxOpenFiles() {
        return maxOpenFiles;
    }

    public DbSettings withMaxOpenFiles(int maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public DbSettings withMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }
}

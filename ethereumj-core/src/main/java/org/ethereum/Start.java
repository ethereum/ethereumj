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
package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.manager.BlockLoader;
import org.ethereum.mine.Ethash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    public static void main(String args[]) {
        CLIInterface.call(args);

        final SystemProperties config = SystemProperties.getDefault();

        getEthashBlockNumber().ifPresent(blockNumber -> createDagFileAndExit(config, blockNumber));

        Ethereum ethereum = EthereumFactory.createEthereum();

        getBlocksDumpPath(config).ifPresent(dumpPath -> loadDumpAndExit(config, dumpPath, ethereum.getBlockLoader()));
    }

    private static void disableSync(SystemProperties config) {
        config.setSyncEnabled(false);
        config.setDiscoveryEnabled(false);
    }

    private static Optional<Long> getEthashBlockNumber() {
        String value = System.getProperty("ethash.blockNumber");
        return isEmpty(value) ? Optional.empty() : Optional.of(parseLong(value));
    }

    /**
     * Creates DAG file for specified block number and terminate program execution with 0 code.
     *
     * @param config      {@link SystemProperties} config instance;
     * @param blockNumber data set block number;
     */
    private static void createDagFileAndExit(SystemProperties config, Long blockNumber) {
        disableSync(config);

        new Ethash(config, blockNumber).getFullDataset();
        // DAG file has been created, lets exit
        System.exit(0);
    }

    private static Optional<Path> getBlocksDumpPath(SystemProperties config) {
        String blocksLoader = config.blocksLoader();

        if (isEmpty(blocksLoader)) {
            return Optional.empty();
        } else {
            Path path = Paths.get(blocksLoader);
            return Files.exists(path) ? Optional.of(path) : Optional.empty();
        }
    }

    /**
     * Loads single or multiple block dumps from specified path, and terminate program execution.<br>
     * Exit code is 0 in case of successfully dumps loading, 1 otherwise.
     *
     * @param config {@link SystemProperties} config instance;
     * @param path   file system path to dump file or directory that contains dumps;
     * @param loader block loader that will be used to import all dumps;
     */
    private static void loadDumpAndExit(SystemProperties config, Path path, BlockLoader loader) {
        disableSync(config);

        boolean loaded = false;
        try {
            Stream<Path> paths = Files.isDirectory(path)
                    ? Files.list(path).sorted()
                    : Stream.of(path);

            loaded = loader.loadBlocks(paths.toArray(Path[]::new));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(loaded ? 0 : 1);
    }
}

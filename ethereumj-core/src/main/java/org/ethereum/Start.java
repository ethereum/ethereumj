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
import org.ethereum.mine.Ethash;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.Long.parseLong;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
public class Start {

    private static void disableSync(SystemProperties config) {
        config.setSyncEnabled(false);
        config.setDiscoveryEnabled(false);
    }

    private static Optional<Long> getEthashBlockNumber() {
        String value = System.getProperty("ethash.blockNumber");
        return isEmpty(value) ? empty() : of(parseLong(value));
    }

    private static Optional<Path> getBlocksDumpPath(SystemProperties config) {
        String blocksLoader = config.blocksLoader();

        if (isEmpty(blocksLoader)) {
            return empty();
        } else {
            Path path = Paths.get(blocksLoader);
            return ofNullable(Files.exists(path) ? path : null);
        }
    }

    public static void main(String args[]) {
        CLIInterface.call(args);

        final SystemProperties config = SystemProperties.getDefault();

        getEthashBlockNumber().ifPresent(ethashBlockNumber -> {
            disableSync(config);
            new Ethash(config, ethashBlockNumber).getFullDataset();
            // DAG file has been created, lets exit
            System.exit(0);
        });

        Ethereum ethereum = EthereumFactory.createEthereum();

        getBlocksDumpPath(config).ifPresent(path -> {
            disableSync(config);
            try {

                Path[] paths;
                if (Files.isDirectory(path)) {
                    paths = Files.list(path).sorted().toArray(Path[]::new);
                } else {
                    paths = new Path[]{path};
                }

                boolean loaded = ethereum.getBlockLoader().loadBlocks(paths);

                if (loaded) {
                    System.out.println(" * Done * ");
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.exit(1);
        });
    }
}

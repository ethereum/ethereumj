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
package org.ethereum.sharding.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;

/**
 * Serves as a provider of validator configuration including {@code pubKey} and withdrawal credentials.
 *
 * <p>
 *     Able to fetch parameters from different sources, check {@link #buildFileConfig()} for details.
 *
 * <p>
 *     <b>Note:</b> has a security drawback by storing plain {@link #depositPrivKey} in config files and memory
 *
 * @author Mikhail Kalinin
 * @since 23.07.2018
 */
public class ValidatorConfig {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static final ValidatorConfig DISABLED = new ValidatorConfig();

    boolean enabled = false;
    byte[] pubKey;
    long withdrawalShard;
    byte[] withdrawalAddress;

    // FIXME do not store private key in memory
    byte[] depositPrivKey;

    public boolean isEnabled() {
        return enabled;
    }

    public byte[] pubKey() {
        return pubKey;
    }

    public long withdrawalShard() {
        return withdrawalShard;
    }

    public byte[] withdrawalAddress() {
        return withdrawalAddress;
    }

    public byte[] depositPrivKey() {
        return depositPrivKey;
    }

    public ValidatorConfig(boolean enabled, byte[] pubKey, long withdrawalShard,
                           byte[] withdrawalAddress, byte[] depositPrivKey) {
        this.enabled = enabled;
        this.pubKey = pubKey;
        this.withdrawalShard = withdrawalShard;
        this.withdrawalAddress = withdrawalAddress;
        this.depositPrivKey = depositPrivKey;
    }

    private ValidatorConfig() {
    }

    public static ValidatorConfig fromFile() {
        try {
            Config fileCfg = buildFileConfig();
            if (!fileCfg.getBoolean("beacon.validator.enabled")) {
                return DISABLED;
            }

            ValidatorConfig config = new ValidatorConfig();
            config.enabled = true;
            config.pubKey = Hex.decode(fileCfg.getString("beacon.validator.pubKey"));
            config.withdrawalShard = fileCfg.getLong("beacon.validator.withdrawal.shard");
            config.withdrawalAddress = Hex.decode(fileCfg.getString("beacon.validator.withdrawal.address"));
            config.depositPrivKey = Hex.decode(fileCfg.getString("beacon.validator.depositPrivKey"));

            return config;
        } catch (Throwable t) {
            logger.error("Failed to build validator config, fall back on DISABLED settings", t);
            return DISABLED;
        }
    }

    private static Config buildFileConfig() {
        Config empty = ConfigFactory.empty();

        Config javaSystemProperties = ConfigFactory.load("no-such-resource-only-system-props");
        Config referenceConfig = ConfigFactory.parseResources("validator.conf");
        logger.info("Validator Config (" + (referenceConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): default properties from resource 'validator.conf'");

        String res = System.getProperty("validator.conf.res");
        Config cmdLineConfigRes = res != null ? ConfigFactory.parseResources(res) : ConfigFactory.empty();
        logger.info("Validator Config (" + (cmdLineConfigRes.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from -Dvalidator.conf.res resource '" + res + "'");

        File userDirFile = new File(System.getProperty("user.dir"), "/config/validator.conf");
        Config userDirConfig = ConfigFactory.parseFile(userDirFile);
        logger.info("Validator Config (" + (userDirConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from file '" + userDirFile + "'");

        String file = System.getProperty("validator.conf.file");
        Config cmdLineConfigFile = file != null ? ConfigFactory.parseFile(new File(file)) : ConfigFactory.empty();
        logger.info("Validator Config (" + (cmdLineConfigFile.entrySet().size() > 0 ? " yes " : " no  ") + "): user properties from -Dethereumj.conf.file file '" + file + "'");

        Config config = empty
                .withFallback(cmdLineConfigFile)
                .withFallback(userDirConfig)
                .withFallback(cmdLineConfigRes)
                .withFallback(referenceConfig);

        logger.debug("Validator Config trace: " + config.root().render(ConfigRenderOptions.defaults().
                setComments(false).setJson(false)));

        config = javaSystemProperties.withFallback(config)
                .resolve();     // substitute variables in config if any

        return config;
    }
}

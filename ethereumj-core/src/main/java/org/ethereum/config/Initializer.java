package org.ethereum.config;

import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.util.BuildInfo;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.io.*;
import java.util.Properties;

/**
 * Created by Anton Nashatyrev on 13.05.2016.
 */
class Initializer implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private CheckDatabaseVersionSafe checkDatabaseVersionSafe = new CheckDatabaseVersionSafe();

    /**
     * Method to be called right after the config is instantiated.
     * Effectively is called before any other bean is initialized
     */
    private void initConfig(SystemProperties config) {
        logger.info("Running {},  core version: {}-{}", config.genesisInfo(), config.projectVersion(), config.projectVersionModifier());
        BuildInfo.printInfo();

        if (config.databaseReset()){
            FileUtil.recursiveDelete(config.databaseDir());
            logger.info("Database reset done");
        }

        // temporary using safe database versioning without reset effect
        checkDatabaseVersionSafe.validateDatabaseVersion(config);

        if (config.getConfig().getBoolean("database.autoResetOldVersion")) {
            FileUtil.recursiveDelete(config.databaseDir());
            logger.info("Auto database reset due to not compatible database version");
        }

        if (logger.isInfoEnabled()) {
            StringBuilder versions = new StringBuilder();
            for (EthVersion v : EthVersion.supported()) {
                versions.append(v.getCode()).append(", ");
            }
            versions.delete(versions.length() - 2, versions.length());
            logger.info("capability eth version: [{}]", versions);
        }
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);
        logger.info("capability bzz version: [{}]", BzzHandler.VERSION);

    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SystemProperties) {
            initConfig((SystemProperties) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * We need to persist the DB version, so after core upgrade we can either reset older incompatible db
     * or make a warning and let the user reset DB manually. Database version is stored in ${database}/version.properties
     *
     * Rules:
     *  - check if database directory exist:
     *    - not exists -> create file with version;
     *    - dir exists -> read version value and compare with required:
     *         - versions are match -> done;
     *         - versions are diff  -> check for special flag:
     *              - flag is true -> remove database directory;
     *              - flag is false -> throw error to user
     */
    public static class CheckDatabaseVersionWithReset {

        public void validateDatabaseVersion(SystemProperties config) {
            final File versionFile = new File(config.databaseDir() + "/version.properties");

            // Detect database version
            final Integer expectedVersion = config.databaseVersion();
            if (isDatabaseDirectoryExists(config)) {
                final Integer actualVersion = getDatabaseVersion(versionFile);
                if (actualVersion.equals(expectedVersion)) {
                    logger.info("Database version is verified and is " + actualVersion);
                } else {
                    handleIncompatibleVersion(config, expectedVersion, actualVersion, versionFile);
                }
            } else {
                putDatabaseVersion(versionFile, config.databaseVersion());
                logger.info("Created database version file");
            }
        }

        protected void handleIncompatibleVersion(SystemProperties config, Integer expectedVersion, Integer actualVersion, File versionFile) {
            if (config.getProperty("database.autoResetOldVersion", false)) {
                FileUtil.recursiveDelete(config.databaseDir());
                logger.info("Detected incompatible database directory: %d", actualVersion);
                logger.warn("Auto reset database directory according to flag");
            } else {
                logger.error("Detected incompatible database version. Detected:%d, required:%d", actualVersion, expectedVersion);
                logger.error("Please remove database directory manually or set `database.autoResetOldVersion` to `true`");
                logger.error("Database directory location is " + config.databaseDir());
                throw new RuntimeException("Incompatible database version " + actualVersion);
            }
        }

        public boolean isDatabaseDirectoryExists(SystemProperties config) {
            final File databaseFile = new File(config.databaseDir());
            return databaseFile.exists() && databaseFile.isDirectory() && databaseFile.list().length > 0;
        }

        /**
         * @return database version stored in specific location in database dir
         *         or 0 if can't detect version due to error
         */
        public Integer getDatabaseVersion(File file) {
            if (!file.exists()) {
                return 0;
            }

            try (Reader reader = new FileReader(file)) {
                Properties prop = new Properties();
                prop.load(reader);
                return Integer.valueOf(prop.getProperty("databaseVersion"));
            } catch (Exception e) {
                logger.error("Problem reading current database version.", e);
                return 0;
            }
        }

        public void putDatabaseVersion(File file, Integer version) {
            file.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(file)) {
                Properties prop = new Properties();
                prop.setProperty("databaseVersion", version.toString());
                prop.store(writer, "Generated database version");
            } catch (Exception e) {
                throw new RuntimeException("Problem writing current database version ", e);
            }
        }
    }

    /**
     * Temp class while all users will have their database version properly set
     */
    public static class CheckDatabaseVersionSafe extends CheckDatabaseVersionWithReset {

        @Override
        protected void handleIncompatibleVersion(SystemProperties config, Integer expectedVersion, Integer actualVersion, File versionFile) {
            if (actualVersion.equals(0)) {
                putDatabaseVersion(versionFile, config.databaseVersion());
                logger.info("Created database version file");
            } else {
                logger.warn("Found database with unknown version " + actualVersion + ". Leaving unchanged.");
            }
        }
    }
}

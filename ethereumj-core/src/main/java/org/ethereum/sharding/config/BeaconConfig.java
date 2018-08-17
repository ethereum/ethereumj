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

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.AbstractCachedSource;
import org.ethereum.datasource.AsyncWriteCache;
import org.ethereum.datasource.BatchSourceWriter;
import org.ethereum.datasource.DbSettings;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.MemSizeEstimator;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.WriteCache;
import org.ethereum.datasource.XorDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.TransactionStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.manager.WorldManager;
import org.ethereum.sharding.processing.BeaconChain;
import org.ethereum.sharding.processing.BeaconChainFactory;
import org.ethereum.sharding.processing.db.BeaconStore;
import org.ethereum.sharding.processing.db.IndexedBeaconStore;
import org.ethereum.sharding.processing.state.BeaconStateRepository;
import org.ethereum.sharding.processing.state.StateRepository;
import org.ethereum.sharding.service.ValidatorRepositoryImpl;
import org.ethereum.sharding.service.ValidatorService;
import org.ethereum.sharding.crypto.DepositAuthority;
import org.ethereum.sharding.contract.DepositContract;
import org.ethereum.sharding.crypto.UnsecuredDepositAuthority;
import org.ethereum.sharding.util.Randao;
import org.ethereum.sharding.service.ValidatorRepository;
import org.ethereum.sharding.service.ValidatorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * In addition to {@link ShardingConfig} bootstraps beacon chain processing.
 *
 * <p>
 *     Enabled only if {@code beacon.enabled} option in config is set to {@code true}.
 *
 * @author Mikhail Kalinin
 * @since 21.07.2018
 */
@Configuration
@Conditional(BeaconConfig.Enabled.class)
public class BeaconConfig {

    @Autowired
    CommonConfig commonConfig;

    @Autowired
    Ethereum ethereum;

    @Autowired
    WorldManager worldManager;

    @Autowired
    DepositContractConfig depositContractConfig;

    @Autowired
    DbFlushManager dbFlushManager;

    @Autowired
    BlockStore blockStore;

    @Autowired
    TransactionStore txStore;

    @Bean
    public ValidatorConfig validatorConfig() {
        return ValidatorConfig.fromFile();
    }

    @Bean
    public ValidatorService validatorService() {
        if (validatorConfig().isEnabled()) {
            return new ValidatorServiceImpl(ethereum, dbFlushManager, validatorConfig(),
                    depositContract(), depositAuthority(), randao());
        } else {
            return new ValidatorService() {};
        }
    }

    @Bean
    public ValidatorRepository validatorRepository() {
        return new ValidatorRepositoryImpl(blockStore, txStore, depositContract());
    }

    @Bean
    public DepositContract depositContract() {
        return new DepositContract(depositContractConfig.getAddress(), depositContractConfig.getBin(),
                depositContractConfig.getAbi());
    }

    @Bean
    public BeaconStore beaconStore() {
        Source<byte[], byte[]> blockSrc = cachedBeaconChainSource("block");
        Source<byte[], byte[]> indexSrc = cachedBeaconChainSource("index");
        return new IndexedBeaconStore(blockSrc, indexSrc);
    }

    @Bean
    public StateRepository beaconStateRepository() {
        Source<byte[], byte[]> src = cachedBeaconChainSource("state");
        return new BeaconStateRepository(src);
    }

    @Bean
    public BeaconChain beaconChain() {
        return BeaconChainFactory.create(commonConfig.dbFlushManager(), beaconStore(), beaconStateRepository());
    }

    @Bean
    @Scope("prototype")
    public Source<byte[], byte[]> cachedBeaconChainSource(String name) {
        AbstractCachedSource<byte[], byte[]> writeCache = new AsyncWriteCache<byte[], byte[]>(beaconChainSource(name)) {
            @Override
            protected WriteCache<byte[], byte[]> createCache(Source<byte[], byte[]> source) {
                WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(source, WriteCache.CacheType.SIMPLE);
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        }.withName(name);
        commonConfig.dbFlushManager().addCache(writeCache);
        return writeCache;
    }

    @Bean
    @Scope("prototype")
    public Source<byte[], byte[]> beaconChainSource(String name) {
        return new XorDataSource<>(beaconChainDbCache(), HashUtil.sha3(name.getBytes()));
    }

    @Bean
    public AbstractCachedSource<byte[], byte[]> beaconChainDbCache() {
        WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(
                new BatchSourceWriter<>(beaconChainDB()), WriteCache.CacheType.SIMPLE);
        ret.setFlushSource(true);
        return ret;
    }

    @Bean
    public DbSource<byte[]> beaconChainDB() {
        DbSettings settings = DbSettings.newInstance();
        return commonConfig.keyValueDataSource("beaconchain", settings);
    }

    public DepositAuthority depositAuthority() {
        return new UnsecuredDepositAuthority(validatorConfig());
    }

    public Randao randao() {
        DbSource<byte[]> src = commonConfig.keyValueDataSource("randao");
        WriteCache.BytesKey<byte[]> cache = new WriteCache.BytesKey<>(
                new BatchSourceWriter<>(src), WriteCache.CacheType.SIMPLE);
        cache.setFlushSource(true);
        dbFlushManager.addCache(cache);

        return new Randao(cache);
    }

    public static class Enabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return SystemProperties.getDefault().processBeaconChain();
        }
    }
}

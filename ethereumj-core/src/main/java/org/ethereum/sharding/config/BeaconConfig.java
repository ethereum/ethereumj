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
import org.ethereum.datasource.BatchSourceWriter;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.WriteCache;
import org.ethereum.db.DbFlushManager;
import org.ethereum.facade.Ethereum;
import org.ethereum.manager.WorldManager;
import org.ethereum.sharding.manager.ShardingWorldManager;
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
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
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

    @Bean
    public ValidatorConfig validatorConfig() {
        return ValidatorConfig.fromFile();
    }

    @Bean @Primary
    public ValidatorService validatorService() {
        if (validatorConfig().isEnabled()) {
            ValidatorService ret = new ValidatorServiceImpl(ethereum, dbFlushManager, validatorConfig(),
                    depositContract(), depositAuthority(), randao());
            ((ShardingWorldManager) worldManager).setValidatorService(ret);
            return ret;
        } else {
            return new ValidatorService() {};
        }
    }

    @Bean
    public ValidatorRepository validatorRepository() {
        return new ValidatorRepository();
    }

    @Bean
    public DepositContract depositContract() {
        return new DepositContract(ethereum, depositContractConfig.getBin(), depositContractConfig.getAbi());
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

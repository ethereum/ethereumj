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
package org.ethereum.config.net;

import org.ethereum.config.blockchain.*;

/**
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public class ETCNetConfig extends BaseNetConfig {
    public static final ETCNetConfig INSTANCE = new ETCNetConfig();

    public ETCNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoNoHFConfig());
        add(2_500_000, new Eip150HFConfig(new DaoNoHFConfig()));
        add(3_000_000, new ETCFork3M(new DaoNoHFConfig()));
    }
}

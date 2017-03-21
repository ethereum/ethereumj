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
public class MainNetConfig extends BaseNetConfig {
    public static final MainNetConfig INSTANCE = new MainNetConfig();

    public MainNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoHFConfig());
        add(2_463_000, new Eip150HFConfig(new DaoHFConfig()));
        add(2_675_000, new Eip160HFConfig(new DaoHFConfig()));
    }
}

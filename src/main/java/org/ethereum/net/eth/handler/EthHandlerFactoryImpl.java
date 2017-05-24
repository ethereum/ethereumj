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
package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.EthVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default factory implementation
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Component
public class EthHandlerFactoryImpl implements EthHandlerFactory {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public EthHandler create(EthVersion version) {
        switch (version) {
            case V62:   return (EthHandler) ctx.getBean("Eth62");
            case V63:   return (EthHandler) ctx.getBean("Eth63");
            default:    throw new IllegalArgumentException("Eth " + version + " is not supported");
        }
    }
}

/*
 * Copyright (c) [2017] [ <ether.camp> ]
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
package org.ethereum.config;

/**
 * Strategy interface to generate the nodeId and the nodePrivateKey.
 * <p>
 * Two strategies are available:
 * <ul>
 * <li>{@link GetNodeIdFromPropsFile}: searches for a nodeId.properties
 * and uses the values in the file to set the nodeId and the nodePrivateKey.</li>
 * <li>{@link GenerateNodeIdRandomly}: generates a nodeId.properties file
 * with a generated nodeId and nodePrivateKey.</li>
 * </ul>
 *
 * @author Lucas Saldanha
 * @see SystemProperties#getGeneratedNodePrivateKey()
 * @since 14.12.2017
 */
public interface GenerateNodeIdStrategy {

    String getNodePrivateKey();

}

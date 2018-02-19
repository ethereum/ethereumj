/*
 * Copyright (c) [2018] [ <ether.camp> ]
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
package org.ethereum.vm.program;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListenerAware;

public interface Storage extends Repository, ProgramListenerAware {

    int getStorageSize(byte[] addr);

    Set<DataWord> getStorageKeys(byte[] addr);

    Map<DataWord, DataWord> getStorage(byte[] addr, Collection<DataWord> keys);

}
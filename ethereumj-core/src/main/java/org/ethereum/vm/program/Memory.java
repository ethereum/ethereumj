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

import java.util.List;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;
import org.ethereum.vm.program.listener.ProgramListenerAware;

public interface Memory extends ProgramListenerAware {

    void setProgramListener(ProgramListener traceListener);

    byte[] read(int address, int size);

    void write(int address, byte[] data, int dataSize, boolean limited);

    void extendAndWrite(int address, int allocSize, byte[] data);

    void extend(int address, int size);

    DataWord readWord(int address);

    // just access expecting all data valid
    byte readByte(int address);

    String toString();

    int size();

    int internalSize();

    List<byte[]> getChunks();

}

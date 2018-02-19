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

import java.util.ArrayList;
import java.util.List;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;

/**
 * A null implementation of the {@link Memory} interface. This is good for
 * passing around if memory is not used, or when testing.
 */
public class NullMemory implements Memory {

    @Override
    public void setProgramListener(ProgramListener traceListener) {
        // Intentionally left blank
    }

    @Override
    public byte[] read(int address, int size) {
        return new byte[size];
    }

    @Override
    public void write(int address, byte[] data, int dataSize, boolean limited) {
        // Intentionally left blank
    }

    @Override
    public void extendAndWrite(int address, int allocSize, byte[] data) {
        // Intentionally left blank
    }

    @Override
    public void extend(int address, int size) {
        // Intentionally left blank
    }

    @Override
    public DataWord readWord(int address) {
        return new DataWord();
    }

    @Override
    public byte readByte(int address) {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int internalSize() {
        return 0;
    }

    @Override
    public List<byte[]> getChunks() {
        return new ArrayList<>(0);
    }

}

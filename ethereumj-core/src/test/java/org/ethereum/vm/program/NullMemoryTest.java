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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.ethereum.vm.trace.ProgramTraceListener;
import org.junit.Test;

/**
 * Test cases for a null implementation of the {@link Memory} interface.
 */
public class NullMemoryTest {
    /**
     * Memory implementation to test.
     */
    private Memory memory = new NullMemory();

    /**
     * Contract:
     * {@link NullMemory#setProgramListener(org.ethereum.vm.program.listener.ProgramListener)}
     * should not throw an exception.
     * 
     * @throws Exception
     */
    @Test
    public void setProgramListenerDoesNotThrowException() throws Exception {
        memory.setProgramListener(new ProgramTraceListener(false));
    }

    /**
     * Contract: {@link NullMemory#read(int, int)} should not return null.
     */
    @Test
    public void readDoesNotReturnNull() {
        assertNotNull(memory.read(0, 0));
        assertEquals(0, memory.read(0, 0).length);
        assertNotNull(memory.read(-10, 65));
        assertEquals(65, memory.read(-10, 65).length);
        assertNotNull(memory.read(0, 72));
        assertEquals(72, memory.read(0, 72).length);
    }

    /**
     * Contract: {@link NullMemory#write(int, byte[], int, boolean)} should not
     * throw an exception.
     * 
     * @throws Exception
     */
    @Test
    public void writeDoesNotThrowException() throws Exception {
        memory.write(0, null, 0, false);
        memory.write(0, null, 0, true);
        memory.write(10, null, 8, false);
        memory.write(0, new byte[0], 0, true);
    }

    /**
     * Contract: {@link NullMemory#extendAndWrite(int, int, byte[])} should not
     * throw an exception.
     * 
     * @throws Exception
     */
    @Test
    public void extendAndWriteDoesNotThrowException() throws Exception {
        memory.extendAndWrite(0, 0, null);
        memory.extendAndWrite(17, 1337, null);
        memory.extendAndWrite(0, 0, new byte[0]);
    }

    /**
     * Contract: {@link NullMemory#extend(int, int)} should not throw an exception.
     * 
     * @throws Exception
     */
    @Test
    public void extendDoesNotThrowException() throws Exception {
        memory.extend(0, 0);
        memory.extend(10, 0);
        memory.extend(0, 10);
        memory.extend(-1, 0);
        memory.extend(0, -1);
    }

    /**
     * Contract: {@link NullMemory#readWord(int)} should not return null.
     */
    @Test
    public void readWordDoesNotReturnNull() {
        assertNotNull(memory.readWord(0));
        assertNotNull(memory.readWord(100));
        assertNotNull(memory.readWord(-1));
    }

    /**
     * Contract: {@link NullMemory#readByte(int)} should return 0.
     */
    @Test
    public void readByteIsEqualToZero() {
        assertEquals(0, memory.readByte(0));
        assertEquals(0, memory.readByte(-1));
        assertEquals(0, memory.readByte(1));
    }

    /**
     * Contract: Size should be zero.
     */
    @Test
    public void sizeIsEqualToZero() {
        assertEquals(0, memory.size());
    }

    /**
     * Contract: Internal size should be zero.
     */
    @Test
    public void internalSizeIsEqualToZero() {
        assertEquals(0, memory.internalSize());
    }

    /**
     * Contract: {@link NullMemory#getChunks()} should return an empty list.
     */
    @Test
    public void getChunksIsAnEmptyList() {
        List<byte[]> list = memory.getChunks();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }
}

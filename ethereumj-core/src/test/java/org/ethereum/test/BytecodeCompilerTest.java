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
package org.ethereum.test;

import org.junit.Assert;
import org.junit.Test;

public class BytecodeCompilerTest {
    @Test
    public void compileSimpleOpcode() {
        BytecodeCompiler compiler = new BytecodeCompiler();

        byte[] result = compiler.compile("ADD");

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(1, result[0]);
    }

    @Test
    public void compileSimpleOpcodeWithSpaces() {
        BytecodeCompiler compiler = new BytecodeCompiler();

        byte[] result = compiler.compile(" ADD ");

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(1, result[0]);
    }

    @Test
    public void compileSimpleOpcodeInLowerCase() {
        BytecodeCompiler compiler = new BytecodeCompiler();

        byte[] result = compiler.compile("add");

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(1, result[0]);
    }

    @Test
    public void compileSimpleOpcodeInMixedCase() {
        BytecodeCompiler compiler = new BytecodeCompiler();

        byte[] result = compiler.compile("Add");

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals(1, result[0]);
    }
}

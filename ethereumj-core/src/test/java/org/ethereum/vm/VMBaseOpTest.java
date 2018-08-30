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
package org.ethereum.vm;

import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvokeMockImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * Base Op test structure
 * Use {@link #compile(String)} with VM code to compile it,
 * then new Program(compiledCode) to run
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class VMBaseOpTest {

    protected ProgramInvokeMockImpl invoke;
    protected Program program;


    @Before
    public void setup() {
        invoke = new ProgramInvokeMockImpl();
    }

    @After
    public void tearDown() {
        invoke.getRepository().close();
    }


    protected byte[] compile(String code) {
        return new BytecodeCompiler().compile(code);
    }
}

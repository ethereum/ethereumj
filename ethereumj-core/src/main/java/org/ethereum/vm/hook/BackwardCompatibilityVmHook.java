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
package org.ethereum.vm.hook;

import org.ethereum.vm.OpCode;
import org.ethereum.vm.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class BackwardCompatibilityVmHook implements VMHook {

    private static final Logger logger = LoggerFactory.getLogger("VM");

    private static VMHook deprecatedHook;
    private final VMHook hook;

    public BackwardCompatibilityVmHook(VMHook hook) {
        this.hook = hook;
    }

    public static void setDeprecatedHook(VMHook deprecatedHook) {
        BackwardCompatibilityVmHook.deprecatedHook = deprecatedHook;
    }

    private static void handleSafe(VMHook hook, Consumer<VMHook> handler) {
        try {
            handler.accept(hook);
        } catch (Exception e) {
            logger.error("VM hook execution error:", e);
        }
    }

    private void handle(Consumer<VMHook> handler) {
        if (nonNull(deprecatedHook)) {
            handleSafe(deprecatedHook, handler);
        }
        handleSafe(hook, handler);
    }

    @Override
    public void startPlay(Program program) {
        handle(hook -> hook.startPlay(program));
    }

    @Override
    public void step(Program program, OpCode opcode) {
        handle(hook -> hook.step(program, opcode));
    }

    @Override
    public void stopPlay(Program program) {
        handle(hook -> hook.stopPlay(program));
    }
}

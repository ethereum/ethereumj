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
package org.ethereum.vm.trace;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.vm.DataWord;

/**
 * Null implementation of the {@link ProgramTrace} interface to be used if
 * program trace is not used
 *
 */
public class NullProgramTrace implements ProgramTrace {

    @Override
    public List<Op> getOps() {
        return new ArrayList<>();
    }

    @Override
    public void setOps(List<Op> ops) {
        // blank intentionally
    }

    @Override
    public String getResult() {
        return new String();
    }

    @Override
    public void setResult(String result) {
        // blank intentionally
    }

    @Override
    public String getError() {
        return new String();
    }

    @Override
    public void setError(String error) {
        // blank intentionally
    }

    @Override
    public String getContractAddress() {
        return new String();
    }

    @Override
    public void setContractAddress(String contractAddress) {
        // blank intentionally
    }

    @Override
    public ProgramTrace result(byte[] result) {
        return this;
    }

    @Override
    public ProgramTrace error(Exception error) {
        return this;
    }

    @Override
    public Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions) {
        return new Op();
    }

    @Override
    public void merge(ProgramTrace programTrace) {
        // blank intentionally
    }

    @Override
    public String asJsonString(boolean formatted) {
        return "{}";
    }

    @Override
    public String toString() {
        return asJsonString(false);
    }
}

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

import java.util.List;

import org.ethereum.vm.DataWord;

public interface ProgramTrace {

    List<Op> getOps();

    void setOps(List<Op> ops);

    String getResult();

    void setResult(String result);

    String getError();

    void setError(String error);

    String getContractAddress();

    void setContractAddress(String contractAddress);

    ProgramTrace result(byte[] result);

    ProgramTrace error(Exception error);

    Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions);

    void merge(ProgramTrace programTrace);

    String asJsonString(boolean formatted);

}
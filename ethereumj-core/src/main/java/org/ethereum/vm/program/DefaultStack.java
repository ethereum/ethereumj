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

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;

public class DefaultStack extends java.util.Stack<DataWord> implements Stack {
    private static final long serialVersionUID = 6781732396941066820L;
    private ProgramListener programListener;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ethereum.vm.program.Stack#setProgramListener(org.ethereum.vm.program.
     * listener.ProgramListener)
     */
    @Override
    public void setProgramListener(ProgramListener listener) {
        this.programListener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Stack#pop()
     */
    @Override
    public synchronized DataWord pop() {
        if (programListener != null)
            programListener.onStackPop();
        return super.pop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Stack#push(org.ethereum.vm.DataWord)
     */
    @Override
    public DataWord push(DataWord item) {
        if (programListener != null)
            programListener.onStackPush(item);
        return super.push(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Stack#swap(int, int)
     */
    @Override
    public void swap(int from, int to) {
        if (isAccessible(from) && isAccessible(to) && (from != to)) {
            if (programListener != null)
                programListener.onStackSwap(from, to);
            DataWord tmp = get(from);
            set(from, set(to, tmp));
        }
    }

    private boolean isAccessible(int from) {
        return from >= 0 && from < size();
    }
}
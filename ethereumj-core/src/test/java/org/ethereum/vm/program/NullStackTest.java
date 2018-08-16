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
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;

import org.ethereum.vm.DataWord;
import org.junit.Test;

public class NullStackTest<T> {
    
    NullStack nullstack = new NullStack();
    
    /*
     * Contract: Nullstack.pop should return new DataWord().
     */
    @Test
    public void popTest() {
	DataWord ret = nullstack.pop();
	assertEquals(new DataWord(), ret);
    }
    
    /*
     * Contract: Nullstack.push should return new DataWord().
     */
    @Test
    public void pushTest() {
	assertEquals(new DataWord(), nullstack.push(new DataWord()));
    }
    
    /*
     * Contract: Nullstack.peek should return new DataWord().
     */
    @Test
    public void peekTest() {
	assertEquals(new DataWord(), nullstack.peek());
    }
    
    /*
     * Contract: Nullstack.size should return new 0.
     */
    @Test
    public void SizeTest() {
	assertEquals(0, nullstack.size());
    }
    
    /*
     * Contract: Nullstack.toArray should not return null value.
     */
    @Test
    public void toArrayTest() {
	assertNotEquals(null, nullstack.toArray());
    }
    
    /*
     * Contract: Nullstack.add should return false in null implementation.
     */
    @Test
    public void addTest() {
	assertEquals(false, nullstack.add(null));
    }
    
    /*
     * Contract: Nullstack.addAll should return false in null implementation.
     */
    @Test
    public void addAllTest() {
	assertEquals(false, nullstack.addAll(null));
    }
    
    /*
     * Contract: Nullstack.addAll should return false in null implementation.
     */
    @Test
    public void addAllTest2() {
	assertEquals(false, nullstack.addAll(0, null));
    }
    
    /*
     * Contract: Nullstack.contains should return false in null implementation.
     */
    @Test
    public void containsTest() {
	assertEquals(false, nullstack.contains(null));
    }
    
    /*
     * Contract: Nullstack.containsAll should return false in null implementation.
     */
    @Test
    public void containsAllTest() {
	assertEquals(false, nullstack.containsAll(null));
    }
    
    /*
     * Contract: Nullstack.indexOf should return 0 in null implementation.
     */
    @Test
    public void indexOfTest() {
	assertEquals(0, nullstack.indexOf(null));
    }
    
    /*
     * Contract: Nullstack.isEmpty should return false in null implementation.
     */
    @Test
    public void isEmpty() {
	assertEquals(false, nullstack.contains(null));
    }
    
    /*
     * Contract: Nullstack.iterator should not return null in null implementation.
     */
    @Test
    public void iteratorTest() {
	assertNotEquals(null, nullstack.iterator());
    }
    
    /*
     * Contract: Nullstack.lastIndexOf should return 0 in null implementation.
     */
    @Test
    public void lastIndexOfTest() {
	assertEquals(0, nullstack.lastIndexOf(null));
    }
    
    /*
     * Contract: Nullstack.removeTest should return false in null implementation.
     */
    @Test
    public void removeTest() {
	assertEquals(false, nullstack.remove(new Object()));
    }
    
    /*
     * Contract: Nullstack.addAll should return false in null implementation.
     */
    @Test
    public void removeTest2() {
	assertEquals(new DataWord(), nullstack.remove(0));
    }
    
    /*
     * Contract: Nullstack.removeAll should return false in null implementation.
     */
    @Test
    public void removeAllTest() {
	assertEquals(false, nullstack.removeAll(null));
    }
    
    /*
     * Contract: Nullstack.retainAll should return false in null implementation.
     */
    @Test
    public void retainAllTest() {
	assertEquals(false, nullstack.retainAll(null));
    }
    
    /*
     * Contract: Nullstack.set should return new DataWord() in null implementation.
     */
    @Test
    public void setTest() {
	assertEquals(new DataWord(), nullstack.set(0, new DataWord()));
    }
    
    /*
     * Contract: Nullstack.subList should return a new ArrayList<>() in null implementation.
     */
    @Test
    public void subListTest() {
	assertEquals(new ArrayList<>(), nullstack.subList(0, 1));
    }

}

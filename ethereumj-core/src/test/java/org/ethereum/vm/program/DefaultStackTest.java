package org.ethereum.vm.program;

import static org.junit.Assert.assertEquals;

import java.util.EmptyStackException;

import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.CompositeProgramListener;
import org.junit.Test;

public class DefaultStackTest {
    @Test
    public void testPush() {
        DefaultStack stack = new DefaultStack();
        assertEquals(0, stack.size());

        stack.push(new DataWord());
        assertEquals(1, stack.size());
    }

    @Test
    public void testPushWithListener() {
        DefaultStack stack = new DefaultStack();
        stack.setProgramListener(new CompositeProgramListener());

        assertEquals(0, stack.size());

        stack.push(new DataWord());
        assertEquals(1, stack.size());
    }

    @Test
    public void testPop() {
        DefaultStack stack = new DefaultStack();
        DataWord a = new DataWord();
        DataWord b = new DataWord();

        stack.push(a);
        stack.push(b);
        assertEquals(2, stack.size());

        assertEquals(b, stack.pop());
        assertEquals(1, stack.size());

        assertEquals(a, stack.pop());
        assertEquals(0, stack.size());
    }

    @Test
    public void testPopWithListener() {
        DefaultStack stack = new DefaultStack();
        stack.setProgramListener(new CompositeProgramListener());
        DataWord a = new DataWord();
        DataWord b = new DataWord();

        stack.push(a);
        stack.push(b);
        assertEquals(2, stack.size());

        assertEquals(b, stack.pop());
        assertEquals(1, stack.size());

        assertEquals(a, stack.pop());
        assertEquals(0, stack.size());
    }

    @Test(expected = EmptyStackException.class)
    public void testPopOutofBounds() {
        DefaultStack stack = new DefaultStack();

        assertEquals(0, stack.size());
        stack.pop();
    }

    @Test(expected = EmptyStackException.class)
    public void testPopOutofBoundsWithListener() {
        DefaultStack stack = new DefaultStack();
        stack.setProgramListener(new CompositeProgramListener());

        assertEquals(0, stack.size());
        stack.pop();
    }

    @Test
    public void testSwap() {
        DefaultStack stack = new DefaultStack();

        DataWord a = new DataWord();
        DataWord b = new DataWord();

        stack.push(a);
        stack.push(b);
        stack.swap(0, 1);

        assertEquals(a, stack.pop());
        assertEquals(b, stack.pop());
    }

    @Test
    public void testSwapWithListener() {
        DefaultStack stack = new DefaultStack();
        stack.setProgramListener(new CompositeProgramListener());

        DataWord a = new DataWord();
        DataWord b = new DataWord();

        stack.push(a);
        stack.push(b);
        stack.swap(0, 1);

        assertEquals(a, stack.pop());
        assertEquals(b, stack.pop());
    }

    @Test
    public void testNotValidSwap() {
        DefaultStack stack = new DefaultStack();
        DataWord a = new DataWord();
        DataWord b = new DataWord();

        stack.push(a);
        stack.push(b);

        stack.swap(0, 0);
        assertEquals(b, stack.peek());

        stack.swap(-1, 0);
        assertEquals(b, stack.peek());

        stack.swap(0, 2);
        assertEquals(b, stack.peek());
    }
}

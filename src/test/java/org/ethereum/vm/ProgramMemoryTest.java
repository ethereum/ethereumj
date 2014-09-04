package org.ethereum.vm;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import org.ethereum.util.ByteUtil;
import org.junit.Before;
import org.junit.Test;

public class ProgramMemoryTest {

	ProgramInvokeMockImpl pi = null;
	Program program;
	ByteBuffer memory;
	
	@Before
	public void createProgram() {
		program = new Program(ByteUtil.EMPTY_BYTE_ARRAY, pi);
	}
	
	@Test
	public void testGetMemSize() {
		ByteBuffer memory = ByteBuffer.allocate(64);
		program.memory = memory;
		assertEquals(64, program.getMemSize());
	}

	@Test
	public void testMemorySave() {

	}

	@Test
	public void testMemoryLoad() {
		fail("Not yet implemented");
	}

	@Test
	public void testMemoryChunk() {
		fail("Not yet implemented");
	}

	@Test
	public void testAllocateMemory1() {

		memory = ByteBuffer.allocate(64);
		int offset = 32;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory2() {

		// memory.limit() > offset, == size
		// memory.limit() < offset + size
		memory = ByteBuffer.allocate(64);
		int offset = 32;
		int size = 64;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory3() {

		// memory.limit() > offset, > size
		memory = ByteBuffer.allocate(64);
		int offset = 0;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory4() {

		memory = ByteBuffer.allocate(64);;
		int offset = 0;
		int size = 64;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory5() {

		memory = ByteBuffer.allocate(64);
		int offset = 0;
		int size = 0;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory6() {

		// memory.limit() == offset, > size
		memory = ByteBuffer.allocate(64);
		int offset = 64;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory7() {

		// memory.limit() == offset - size
		memory = ByteBuffer.allocate(64);
		int offset = 96;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(128, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory8() {

		memory = ByteBuffer.allocate(64);
		int offset = 0;
		int size = 96;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory9() {

		// memory.limit() < offset, > size
		// memory.limit() < offset - size
		memory = ByteBuffer.allocate(64);
		int offset = 96;
		int size = 0;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}
	
	/************************************************/
	
	
	@Test
	public void testAllocateMemory10() {

		// memory = null, offset > size
		int offset = 32;
		int size = 0;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(32, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory11() {

		// memory = null, offset < size
		int offset = 0;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(32, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory12() {

		// memory.limit() < offset, < size
		memory = ByteBuffer.allocate(32);
		int offset = 64;
		int size = 96;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(160, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory13() {

		// memory.limit() > offset, < size
		memory = ByteBuffer.allocate(64);
		int offset = 32;
		int size = 128;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(160, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemory14() {

		// memory.limit() < offset, == size
		memory = ByteBuffer.allocate(64);
		int offset = 96;
		int size = 64;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(160, program.getMemSize());
	}

	@Test
	public void testAllocateMemory15() {

		// memory.limit() == offset, < size
		memory = ByteBuffer.allocate(64);
		int offset = 64;
		int size = 96;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(160, program.getMemSize());
	}

	@Test
	public void testAllocateMemory16() {

		// memory.limit() == offset, == size
		// memory.limit() > offset - size
		memory = ByteBuffer.allocate(64);
		int offset = 64;
		int size = 64;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(128, program.getMemSize());
	}

	@Test
	public void testAllocateMemory17() {

		// memory.limit() > offset + size
		memory = ByteBuffer.allocate(96);
		int offset = 32;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}

	@Test
	public void testAllocateMemoryUnrounded1() {

		// memory unrounded 
		memory = ByteBuffer.allocate(16);
		int offset = 64;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemoryUnrounded2() {

		// offset unrounded 
		memory = ByteBuffer.allocate(32);
		int offset = 16;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemoryUnrounded3() {

		// size unrounded 
		memory = ByteBuffer.allocate(32);
		int offset = 64;
		int size = 16;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(96, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemoryUnrounded4() {

		// memory + offset unrounded 
		memory = ByteBuffer.allocate(16);
		int offset = 16;
		int size = 32;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64	, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemoryUnrounded5() {

		// memory + size unrounded 
		memory = ByteBuffer.allocate(16);
		int offset = 32;
		int size = 16;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(64, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemoryUnrounded6() {

		// offset + size unrounded 
		memory = ByteBuffer.allocate(32);
		int offset = 16;
		int size = 16;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(32, program.getMemSize());
	}
	
	@Test
	public void testAllocateMemoryUnrounded7() {

		// memory + offset + size unrounded 
		memory = ByteBuffer.allocate(16);
		int offset = 16;
		int size = 16;
		program.memory = memory;
		program.allocateMemory(offset, size);
		assertEquals(32,program.getMemSize());
	}	
}

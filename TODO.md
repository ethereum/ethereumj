

##### TODO list:
----------------

- [x] **VM execution:** support CALL op   
- [x] **VM execution:** support CALL op with in/out data   
- [ ] **VM execution:** support CREATE op
- [ ] **SerpentCompiler** compile create(gas, mem_start, mem_size)
- [ ] **SerpentCompiler** compile create(gas, mem_start, import("examples/mul2.se"))                                  
- [ ] **SerpentCompiler** compile return(array) correct
- [ ] **VM execution:** SUICIDE op adjust
- [ ] **ProgramPlayDialog** support internal calls
- [ ] **Build:** extract core module and studio application
- [ ] **Performance:** BigInteger math change for constant arrays implementation   
economy for memory allocation
- [ ] **Command Line:** add the headless run option   
- [ ] **Testing by JSON files:** follow cpp client performs test case by getting json file contains the test describe
- [ ] **SerpentCompiler** Serpent new syntax:   
(@> @< @/ @%) - unsigned operations   
 > < / % - default are all signed operations   
+= -= *= /= %= @/= @%= - short form operations      
share - code section
    
- [ ] **LLL_to_ASM compiler** list style language to EVM assembly compiler:    

- 

##### UnitTest:   
----------------

- [x] **VM complex:** CALL testing for in arrays
- [x] **VM complex:** CALL testing for out result
- [ ] **VM complex:** CREATE testing 
- [ ] **VM complex:** SUICIDE testing
- [ ] **SerpentCompiler** compile return(array) correct
- [ ] **WorldManager** apply transactions


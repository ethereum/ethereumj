
The core library API for Ethereum project can be included
into any other Java/Scala project by simple maven 
script include: 

```
<dependency>
   <groupId>org.ethereum</groupId>
   <artifactId>ethereumj</artifactId>
   <version>0.5.7</version>
   <type>jar</type>
</dependency>
```


EthereumJ release repository can be found here: 
 * https://bintray.com/ethereum/maven/org.ethereum/view


The showcase for ethereumj-core usage can be found in [ethereumj-studio](../ethereumj-studio)
 
######  :small_blue_diamond: Build instructions (maven)
  1. build_1:  [no test run] , [released to local repository] : ~> ` mvn clean install -Dmaven.test.skip=true `   
  2. build_2:  [include test run] , [released to local repository] : ~> ` mvn clean install  `   
 
######  :small_blue_diamond: release instructions (ant) (!) credential required
  1. ` mvn install ` - which release the lib to a local repositroy
  2. after the release - ` ant -f bintray-publish-version.xml `
  

  
 

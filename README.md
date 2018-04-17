# Welcome to ethereumj

[![Slack Status](http://harmony-slack-ether-camp.herokuapp.com/badge.svg)](http://ether.camp) 
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ethereum/ethereumj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ethereum/ethereumj.svg?branch=master)](https://travis-ci.org/ethereum/ethereumj)
[![Coverage Status](https://coveralls.io/repos/ethereum/ethereumj/badge.png?branch=master)](https://coveralls.io/r/ethereum/ethereumj?branch=master)


# About
EthereumJ is a pure-Java implementation of the Ethereum protocol. For high-level information about Ethereum and its goals, visit [ethereum.org](https://ethereum.org). The [ethereum white paper](https://github.com/ethereum/wiki/wiki/White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

We keep EthereumJ as thin as possible. For [JSON-RPC](https://github.com/ethereum/wiki/wiki/JSON-RPC) support and other client features check [Ethereum Harmony](https://github.com/ether-camp/ethereum-harmony).

# Running EthereumJ

##### Adding as a dependency to your Maven project: 

```
   <dependency>
     <groupId>org.ethereum</groupId>
     <artifactId>ethereumj-core</artifactId>
     <version>1.6.3-RELEASE</version>
   </dependency>
```

##### or your Gradle project: 

```
   repositories {
       mavenCentral()
       jcenter()
       maven { url "https://dl.bintray.com/ethereum/maven/" }
   }
   compile "org.ethereum:ethereumj-core:1.6.+"
```

As a starting point for your own project take a look at https://github.com/ether-camp/ethereumj.starter

##### Building an executable JAR
```
git clone https://github.com/ethereum/ethereumj
cd ethereumj
cp ethereumj-core/src/main/resources/ethereumj.conf ethereumj-core/src/main/resources/user.conf
vim ethereumj-core/src/main/resources/user.conf # adjust user.conf to your needs
./gradlew clean shadowJar
java -jar ethereumj-core/build/libs/ethereumj-core-*-all.jar
```

##### Running from command line:
```
> git clone https://github.com/ethereum/ethereumj
> cd ethereumj
> ./gradlew run [-PmainClass=<sample class>]
```

##### Optional samples to try:
```
./gradlew run -PmainClass=org.ethereum.samples.BasicSample
./gradlew run -PmainClass=org.ethereum.samples.FollowAccount
./gradlew run -PmainClass=org.ethereum.samples.PendingStateSample
./gradlew run -PmainClass=org.ethereum.samples.PriceFeedSample
./gradlew run -PmainClass=org.ethereum.samples.PrivateMinerSample
./gradlew run -PmainClass=org.ethereum.samples.TestNetSample
./gradlew run -PmainClass=org.ethereum.samples.TransactionBomb
```

##### Importing project to IntelliJ IDEA: 
```
> git clone https://github.com/ethereum/ethereumj
> cd ethereumj
> gradlew build
```
  IDEA: 
* File -> New -> Project from existing sources…
* Select ethereumj/build.gradle
* Dialog “Import Project from gradle”: press “OK”
* After building run either `org.ethereum.Start`, one of `org.ethereum.samples.*` or create your own main. 

# Configuring EthereumJ

For reference on all existing options, their description and defaults you may refer to the default config `ethereumj.conf` (you may find it in either the library jar or in the source tree `ethereum-core/src/main/resources`) 
To override needed options you may use one of the following ways: 
* put your options to the `<working dir>/config/ethereumj.conf` file
* put `user.conf` to the root of your classpath (as a resource) 
* put your options to any file and supply it via `-Dethereumj.conf.file=<your config>`
* programmatically by using `SystemProperties.CONFIG.override*()`
* programmatically using by overriding Spring `SystemProperties` bean 

Note that don’t need to put all the options to your custom config, just those you want to override. 

# Special thanks
YourKit for providing us with their nice profiler absolutely for free.

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

# Contact
Chat with us via [Gitter](https://gitter.im/ethereum/ethereumj)

# License
ethereumj is released under the [LGPL-V3 license](LICENSE).


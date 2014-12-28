
## ethereum-core

### Include ethereum-core in your project

 1. Add http://dl.bintray.com/ethereum/maven as a repository to your build script
 2. Add a dependency on `org.ethereum:ethereumj:$version`, where `$version` is one of those listed at https://bintray.com/ethereum/maven/org.ethereum/view


### Examples

See [ethereumj-studio](../ethereumj-studio).


### Build from source

#### Compile, test and package

Run `../gradlew build`.

 - find jar artifacts at `build/libs`
 - find unit test and code coverage reports at `build/reports`

#### Run an ethereum node

 - run `../gradlew run`, or
 - build a standalone executable jar with `../gradlew shadow` and execute the `-all` jar in `build/libs` using `java -jar [jarfile]`.

#### Import sources into IntelliJ IDEA

Use IDEA 14 or better and import project based on Gradle sources.

Note that in order to build the project without errors in IDEA, you will need to run `gradle antlr4` manually.

#### Install artifacts into your local `~/.m2` repository

Run `../gradlew install`.

#### Publish ethereumj-core builds

_TODO: integrate bintray gradle plugin_

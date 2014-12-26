
## ethereum-core

### Include ethereum-core in your project

 1. Add http://dl.bintray.com/ethereum/maven as a repository to your build script
 2. Add a dependency on `org.ethereum:ethereumj:$version`, where `$version` is one of those listed at https://bintray.com/ethereum/maven/org.ethereum/view


### Examples

See [ethereumj-studio](../ethereumj-studio).


### Build from source

#### Compile, test and package

Run `./gradlew build`.

 - find jar artifacts at `build/libs`
 - find unit test and code coverage reports at `build/reports`

#### Run an ethereum node

 - run `./gradlew run`, or
 - build a standalone executable jar with `./gradlew shadow` and execute the `-all` jar in `build/libs` using `java -jar [jarfile]`.

#### Publish ethereumj-core builds

_TODO: integrate bintray gradle plugin_

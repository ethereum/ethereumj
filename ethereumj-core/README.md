
## ethereum-core

### Include ethereum-core in your project

#### For snapshot builds:

 - Add https://oss.jfrog.org/libs-snapshot/ as a repository to your build script
 - Add a dependency on `org.ethereum:ethereumj-core:${VERSION}`, where `${VERSION}` is of the form `0.7.14-SNAPSHOT`.

Example:

    <repository>
        <id>jfrog-snapshots</id>
        <name>oss.jfrog.org</name>
        <url>https://oss.jfrog.org/libs-snapshot/</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
    <!-- ... -->
    <dependency>
       <groupId>org.ethereum</groupId>
       <artifactId>ethereumj-core</artifactId>
       <version>0.7.14-SNAPSHOT</version>
    </dependency>

#### For release builds:

_There are no release builds at this time. Use snapshots in the meantime._


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

Push to master.

Run `../gradlew release`. Snapshots will be published to oss.jfrog.org; releases will be published to Bintray. You must supply the `bintrayUser` and `bintrayKey` properties to the Gradle build in order to authenticate. Do this in $HOME/.gradle/gradle.properties for greatest convenience and security.

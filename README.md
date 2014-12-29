# Welcome to ethereumj 
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/ethereum/ethereumj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ethereum/ethereumj.svg?branch=master)](https://travis-ci.org/ethereum/ethereumj)
[![Coverage Status](https://coveralls.io/repos/ethereum/ethereumj/badge.png?branch=master)](https://coveralls.io/r/ethereum/ethereumj?branch=master)
[![Stories in Progress](https://badge.waffle.io/ethereum/ethereumj.png?title=In%20Progress&label=in_progress)](https://waffle.io/ethereum/ethereumj)

![Now hiring](http://i.imgur.com/lJw1Tui.jpg)

# About
ethereumj is a pure-Java implementation of the Ethereum protocol. For high-level information about Ethereum and its goals, visit [ethereum.org](https://ethereum.org). The [ethereum white paper](https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

This repository consists of:
 * [ethereum-core](ethereumj-core): a library suitable for inclusion in any JVM-based project.
 * [ethereum-studio](ethereumj-studio): a simple GUI for exploring Ethereum functionality and usage of the ethereumj API.

To see ethereum-studio in action, watch this [video](https://youtu.be/D5ok7jh7AOg).

# Todo
The Ethereum protocol is under heavy development, thus so is this implementation. See the [todo list](TODO.md), GitHub [Issues](https://github.com/ethereum/ethereumj/issues) and [milestone schedule](https://github.com/ethereum/ethereumj/milestones). Issues are prioritized using [waffle](http://waffle.io/ethereum/ethereumj).

# Contact
Chat with us via [Gitter](https://gitter.im/ethereum/ethereumj) or [#ethereumj](webchat.freenode.net/?channels=ethereumj) on Freenode.

# Building from source

 - Clone this repository and run `./gradlew build` (or at least `./gradlew antlr4` to generate sources).
 - Import all sources into IntelliJ IDEA (14+) with `File->Import project` and point to the top-level `build.gradle` file.
 - Make sure to set your language level in `File->Project Structure...` to JDK 8.
 - Run `Build->Make Project`. When complete, there should be no errors.

# Usage
For complete details on downloading, building and using [etherumj-core](ethereumj-core) and [ethereumj-studio](ethereumj-studio), see their respective readme files.

# License
ethereumj is released under the [MIT license](LICENSE).

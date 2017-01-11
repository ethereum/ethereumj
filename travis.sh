#!/bin/bash
# Installs Oracle JDK 7
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/7u79-b15/jdk-7u79-linux-x64.tar.gz
tar -zxvf jdk-7u79-linux-x64.tar.gz
rm jdk-7u79-linux-x64.tar.gz
mv jdk1.7.0_79 $HOME/.jdk
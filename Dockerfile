FROM frolvlad/alpine-oraclejdk8

RUN apk add --no-cache ca-certificates zsh curl git

ENV SHELL /bin/zsh

RUN mkdir /ethereumj
WORKDIR /ethereumj

ADD . /ethereumj

CMD "./gradlew run"
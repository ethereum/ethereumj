FROM gizmotronic/oracle-java:java8

RUN apt-get update  && \
    apt-get install -y git curl zsh

ENV SHELL /bin/zsh

RUN mkdir /ethereumj
WORKDIR /ethereumj

COPY . /ethereumj

CMD "./gradlew --no-daemon run"

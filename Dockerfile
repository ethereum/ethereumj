FROM gizmotronic/oracle-java:java8

RUN apt-get update  && \
    apt-get install -y git curl zsh

ENV SHELL /bin/zsh

RUN mkdir /ethereumj
WORKDIR /ethereumj

COPY . /ethereumj

RUN ./gradlew --no-daemon build -x test

CMD "./gradlew --no-daemon run"

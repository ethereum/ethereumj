FROM openjdk:8u181-slim as builder

# ARGS
ARG ETHEREUMJ_MAIN_CLASS='io.enkrypt.kafka.EthereumKafkaStarter'

# Install deps
RUN apt update && \
  apt install -y wget git && \
  apt-get clean && \
  apt-get autoremove

# Create workdir
RUN mkdir /tmp/ethereumj
WORKDIR /tmp/ethereumj

# Copy src
COPY . /tmp/ethereumj

# Download dumb-init
RUN wget -O /dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.2/dumb-init_1.2.2_amd64

# Build Ethereumj
RUN /tmp/ethereumj/gradlew --no-daemon assemble -PmainClass=${ETHEREUMJ_MAIN_CLASS}

FROM openjdk:8u181-jre-slim

ARG DEFAULT_JVM_ARGS='-server -Xss2m -Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseG1GC -XX:-OmitStackTraceInFastThrow'

ENV JAVA_OPTS=${DEFAULT_JVM_ARGS}

# Copy to new image
COPY --from=builder /tmp/ethereumj/ethereumj-core/build/distributions/ethereumj-core-*-RELEASE.tar /
COPY --from=builder /dumb-init /usr/bin/dumb-init

# Prepare binaries
RUN ls *.tar | xargs -n1 tar -xvf && \
  rm -rf /ethereumj-core-*.tar && \
  mv /ethereumj-core-* /usr/bin/ethereumj && \
  chmod +x /usr/bin/dumb-init && \
  chmod +x /usr/bin/ethereumj/bin/ethereumj-core

# Ports
EXPOSE 30303

# Expose volume
VOLUME /data

# Define entry
ENTRYPOINT ["/usr/bin/dumb-init", "--"]

# Define cmd
CMD ["/usr/bin/ethereumj/bin/ethereumj-core"]

FROM eclipse-temurin:8-jdk-jammy AS builder

COPY . /java-agent
WORKDIR /java-agent

RUN useradd -m -s /bin/bash dev && \
    chown -R dev:dev /java-agent

USER dev
RUN ./gradlew -g .gradle --no-daemon clean build -x test

FROM alpine:latest

USER root
COPY --from=builder /java-agent/agent/build/libs/agent-*-all.jar /data/opentelemetry-javaagent.jar

FROM openjdk:8-jre-alpine
COPY ./ ./

RUN apk --no-cache add curl

ENV JVM_ARGS=${JVM_ARGS}

COPY target/feature-probe-api-1.1.0.jar feature-probe-api-1.1.0.jar



ENTRYPOINT java ${JVM_ARGS} -jar feature-probe-api-1.1.0.jar

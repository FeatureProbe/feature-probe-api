FROM openjdk:8-jre-alpine
COPY ./ ./

RUN apk --no-cache add curl

ENV JVM_ARGS=${JVM_ARGS}

COPY feature-probe-admin/target/feature-probe-admin-1.0.0.jar feature-probe-admin-1.0.0.jar



ENTRYPOINT java ${JVM_ARGS} -jar feature-probe-admin-1.0.0.jar

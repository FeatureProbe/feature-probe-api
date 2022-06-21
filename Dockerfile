FROM openjdk:8-jre-alpine
COPY ./ ./

RUN apk --no-cache add curl

COPY target/feature-probe-api-1.1.0.jar feature-probe-api-1.1.0.jar

ENTRYPOINT ["java", "-jar", "feature-probe-api-1.1.0.jar"]

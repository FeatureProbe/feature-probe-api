FROM maven:3.6.1-jdk-8-alpine AS builder 
COPY ./ ./

RUN mvn clean package 

FROM openjdk:8-jre-alpine
RUN apk --no-cache add curl

COPY --from=builder target/feature-probe-api-1.0.1.jar feature-probe-api-1.0.1.jar 

ENTRYPOINT ["java", "-jar", "feature-probe-api-1.0.1.jar"]

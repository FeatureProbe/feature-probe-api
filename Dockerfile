FROM openjdk:8-jre-alpine
RUN apk --no-cache add curl

COPY ./ ./

RUN ./mvnw package

ADD target/feature-probe-api-1.0.1.jar feature-probe-api-1.0.1.jar
EXPOSE 4008
ENTRYPOINT ["java", "-jar", "feature-probe-api-1.0.1.jar"]

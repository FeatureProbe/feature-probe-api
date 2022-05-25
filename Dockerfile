FROM openjdk:8
ADD target/feature-probe-api-1.0-SNAPSHOT.jar feature-probe-api-1.0-SNAPSHOT.jar
EXPOSE 4008
ENTRYPOINT ["java", "-jar", "feature-probe-api-1.0-SNAPSHOT.jar"]
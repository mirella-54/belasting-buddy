FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/hour-tracking-service-*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]

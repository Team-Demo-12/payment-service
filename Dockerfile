FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app
COPY --from=build /app/target/payment-service-*.jar /app/app.jar
ENV PORT=8080 \
    SERVICE_NAME=payment-service \
    SERVICE_VERSION=4.17.3
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
    CMD python3 -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8080/health')" || exit 1
CMD ["java", "-jar", "/app/app.jar"]

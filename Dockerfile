# Stage 1
FROM clojure:latest AS builder
WORKDIR /app
COPY project.clj .
RUN lein deps

COPY src/ src/
RUN lein uberjar

# Stage 2
FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/target/uberjar/glados-0.1.0-SNAPSHOT-standalone.jar /app/app.jar
EXPOSE 3000
CMD ["java", "-jar", "/app/app.jar"]
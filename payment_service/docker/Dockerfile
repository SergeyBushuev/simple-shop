FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY ../.. .
RUN gradle :payment_service:build --no-daemon -x test

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/payment_service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
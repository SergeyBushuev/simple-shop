FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY ../.. .
RUN gradle :simple_shop:build -x test

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/simple_shop/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
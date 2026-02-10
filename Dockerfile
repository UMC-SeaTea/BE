FROM gradle:jdk17-focal AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN ./gradlew build --no-daemon --build-cache -x test || true
COPY src ./src
RUN ./gradlew build --no-daemon --build-cache -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
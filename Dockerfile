FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle /app/
COPY gradle /app/gradle
COPY src /app/src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/build/libs/api-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

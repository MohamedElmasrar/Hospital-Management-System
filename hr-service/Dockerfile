FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew dependencies --no-daemon

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew bootJar -x test --no-daemon


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8002
EXPOSE 9091

ENTRYPOINT [ "java", "-jar", "app.jar" ]

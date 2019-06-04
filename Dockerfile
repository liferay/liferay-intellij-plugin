FROM openjdk:8-stretch

CMD ["mkdir", "-p", "/root/.gradle/"]

COPY . /src

WORKDIR /src

CMD ["./gradlew", "clean", "test", "publishPlugin"]
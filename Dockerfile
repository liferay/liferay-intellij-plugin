FROM openjdk:17-alpine

CMD ["mkdir", "-p", "/root/.gradle/"]

COPY . /liferay-intellij-plugin

WORKDIR /liferay-intellij-plugin

CMD ["./gradlew", "clean", "-x", "test", "publishPlugin"]
FROM openjdk:17-alpine

RUN apk update && apk add git

CMD ["mkdir", "-p", "/root/.gradle/"]

COPY . /liferay-intellij-plugin

WORKDIR /liferay-intellij-plugin

CMD ["./gradlew", "clean", "-x", "test", "publishPlugin"]
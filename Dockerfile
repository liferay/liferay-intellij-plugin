FROM azul/zulu-openjdk-alpine:21

RUN apk update && apk add git
RUN ["mkdir", "-p", "/root/.gradle/"]

COPY . /liferay-intellij-plugin

WORKDIR /liferay-intellij-plugin

CMD ["./gradlew", "clean", "-x", "test", "publishPlugin"]
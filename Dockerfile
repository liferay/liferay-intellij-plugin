FROM openjdk:8-stretch

CMD ["mkdir", "-p", "/root/.gradle/"]

COPY . /liferay-intellij-plugin

WORKDIR /liferay-intellij-plugin

CMD ["./gradlew", "clean", "test", "publishPlugin"]
### STAGE 1: Build ###
# FROM adoptopenjdk/maven-openjdk11:latest AS build
FROM royalwang/maven-openjdk:jdk-11-3.6.3 AS build
RUN mkdir -p /root/.m2 \
    && mkdir /root/.m2/repository
COPY settings.xml /root/.m2
WORKDIR /tmp/
COPY pom.xml ./
RUN mkdir /tmp/src
COPY src /tmp/src
RUN mvn -Dmaven.wagon.http.ssl.insecure=true \
        -Dmaven.wagon.http.ssl.allowall=true \
        -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
         clean compile package
### STAGE 2: Run ###
# FROM fabric8/java-alpine-openjdk11-jre as RUN
FROM arm64v8/openjdk:11-jre as RUN
VOLUME /tmp
COPY --from=build /tmp/target/app.jar /
EXPOSE 8080/tcp 8081/tcp
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]


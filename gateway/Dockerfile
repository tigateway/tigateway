### STAGE 1: Build ###
FROM royalwang/maven-prebuild-jdk11:spring-boot-2.6.3 AS build

RUN mkdir -p /root/.m2 \
    && mkdir -p /root/.m2/repository
COPY ./settings.xml /root/.m2

WORKDIR /build
COPY . /build

RUN mvn -Dmaven.wagon.http.ssl.insecure=true \
        -Dmaven.wagon.http.ssl.allowall=true \
        -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
        -DskipTests \
        --batch-mode \
        clean compile package


### STAGE 2: Run ###
FROM arm64v8/openjdk:11-jre as RUN
ENV TZ=Asia/Shanghai
ENV PROJECT=gateway

WORKDIR /app
VOLUME /app

COPY --from=build /build/${PROJECT}/target/*.jar /app/${PROJECT}/

RUN cd /app/; \
    find . -type d -mindepth 1 -maxdepth 1|while read dname; do \
      find $dname -name "*.jar" -mindepth 1 -maxdepth 1|while read fname; do \
        echo "$fname" "$dname"; mv "$fname" "$dname"/app.jar; break 1; done; done

EXPOSE 8090/tcp
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /app/${PROJECT}/app.jar" ]


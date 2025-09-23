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
FROM openjdk:11-jre as RUN
ENV TZ=Asia/Shanghai
ENV PROJECT=gateway

# 安装kubectl用于Kubernetes集成
RUN apt-get update && apt-get install -y \
    curl \
    && curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
    && chmod +x kubectl \
    && mv kubectl /usr/local/bin/ \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/${PROJECT}/target/*.jar /app/

WORKDIR /app
RUN cd /app/; \
    find . -name "*.jar" -mindepth 1 -maxdepth 1|while read fname; do \
      echo "$fname"; mv "$fname" app.jar; break 1;  \
    done

# 创建必要的目录
RUN mkdir -p /app/config /app/logs

VOLUME /app

EXPOSE 8080/tcp 8090/tcp

# 设置JVM参数以支持Kubernetes环境
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=kubernetes \
    -Dspring.cloud.kubernetes.config.enabled=true \
    -Dspring.cloud.kubernetes.discovery.enabled=true \
    -Xmx512m -Xms256m \
    -jar /app/app.jar" ]


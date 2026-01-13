### STAGE 1: Build ###
FROM maven:3.9-eclipse-temurin-17 AS build

RUN mkdir -p /root/.m2 \
    && mkdir -p /root/.m2/repository

WORKDIR /build

# 复制 settings.xml
COPY settings.xml /root/.m2/settings.xml

# 复制整个项目（从项目根目录构建）
COPY . /build

# 构建项目（从项目根目录执行）
RUN mvn -pl ti-gateway-kubernetes -am \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true \
    -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
    -DskipTests \
    --batch-mode \
    clean package

### STAGE 2: Run ###
FROM eclipse-temurin:17-jre
ENV TZ=Asia/Shanghai

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
    && chmod +x kubectl \
    && mv kubectl /usr/local/bin/ \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 复制构建的 JAR 文件
COPY --from=build /build/ti-gateway-kubernetes/target/*.jar /app/app.jar

# 创建必要的目录
RUN mkdir -p /app/config /app/logs

WORKDIR /app

VOLUME /app/config
VOLUME /app/logs

# 暴露端口：8080 (Gateway), 8081 (Admin), 8090 (Management)
EXPOSE 8080/tcp 8081/tcp 8090/tcp

# 设置JVM参数以支持Kubernetes环境
ENTRYPOINT [ "sh", "-c", "java \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=kubernetes \
    -Dspring.cloud.kubernetes.config.enabled=true \
    -Dspring.cloud.kubernetes.discovery.enabled=true \
    -Xmx1024m -Xms512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -jar /app/app.jar" ]


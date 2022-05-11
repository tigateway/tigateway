FROM royalwang/maven-openjdk:jdk-11-3.6.3 AS builder

COPY pom.xml /pom/
COPY settings.xml /pom/
COPY gateway/pom.xml /pom/gateway/
COPY scg-operator/pom.xml /pom/scg-operator/
COPY gateway-kubernetes-extensions/pom.xml /pom/gateway-kubernetes-extensions/

WORKDIR /pom
RUN mvn dependency:go-offline -s ./settings.xml

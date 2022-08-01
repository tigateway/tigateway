FROM openjdk:8-jdk-alpine
COPY ./target/gateway-dashboard-1.0.0.jar /app/
WORKDIR /app
ENV PARAMS=""
ENTRYPOINT ["sh","-c","java -jar gateway-dashboard-1.0.0.jar $PARAMS"]

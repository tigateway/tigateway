# TiGateway Development Environment Setup Guide

## Overview

This document provides a comprehensive guide for setting up the TiGateway development environment, including basic requirements, development tools, quick start steps, and detailed configuration instructions.

## Basic Requirements

### System Requirements

- **Java**: JDK 11 or higher
- **Maven**: 3.6+ 
- **Docker**: 20.10+ (for containerized development)
- **Kubernetes**: 1.20+ (for Kubernetes integration)
- **Helm**: 3.0+ (for Helm chart development)

### Development Tools

- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**: 2.20+ for version control
- **Node.js**: 16+ (for documentation and frontend development)

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/tigateway/tigateway.git
cd tigateway
```

### 2. Build Project

```bash
mvn clean compile
```

### 3. Run Tests

```bash
mvn test
```

### 4. Start Application

```bash
mvn spring-boot:run -pl ti-gateway-kubernetes
```

## Detailed Setup Instructions

### Java Development Environment

#### Install Java 11+

**macOS (using Homebrew):**
```bash
brew install openjdk@11
export JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-11-jdk
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

**Windows:**
1. Download OpenJDK 11 from [Adoptium](https://adoptium.net/)
2. Install and set `JAVA_HOME` environment variable
3. Add `%JAVA_HOME%\bin` to PATH

#### Verify Java Installation

```bash
java -version
javac -version
echo $JAVA_HOME
```

### Maven Configuration

#### Install Maven

**macOS (using Homebrew):**
```bash
brew install maven
```

**Ubuntu/Debian:**
```bash
sudo apt install maven
```

**Windows:**
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract to a directory (e.g., `C:\apache-maven-3.8.6`)
3. Set `MAVEN_HOME` environment variable
4. Add `%MAVEN_HOME%\bin` to PATH

#### Configure Maven Settings

Create `~/.m2/settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <mirrors>
        <!-- Aliyun Maven Mirror for faster downloads in China -->
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <name>Aliyun Maven</name>
            <url>https://maven.aliyun.com/repository/central</url>
        </mirror>
    </mirrors>
    
    <profiles>
        <profile>
            <id>jdk-11</id>
            <activation>
                <activeByDefault>true</activeByDefault>
                <jdk>11</jdk>
            </activation>
            <properties>
                <maven.compiler.source>11</maven.compiler.source>
                <maven.compiler.target>11</maven.compiler.target>
                <maven.compiler.compilerVersion>11</maven.compiler.compilerVersion>
            </properties>
        </profile>
    </profiles>
</settings>
```

#### Verify Maven Installation

```bash
mvn -version
```

### IDE Configuration

#### IntelliJ IDEA Setup

1. **Install IntelliJ IDEA Community or Ultimate Edition**

2. **Import Project:**
   - Open IntelliJ IDEA
   - Select "Open or Import"
   - Choose the TiGateway project directory
   - Select "Import project from external model" → "Maven"
   - Click "Next" and "Finish"

3. **Configure Project SDK:**
   - Go to File → Project Structure → Project
   - Set Project SDK to Java 11
   - Set Project language level to 11

4. **Install Plugins:**
   - Lombok Plugin
   - Spring Boot Plugin
   - Kubernetes Plugin
   - Docker Plugin

5. **Configure Code Style:**
   - Go to File → Settings → Editor → Code Style → Java
   - Import the project's code style configuration

#### Eclipse Setup

1. **Install Eclipse IDE for Java Developers**

2. **Import Project:**
   - File → Import → Existing Maven Projects
   - Browse to TiGateway project directory
   - Select all modules and click "Finish"

3. **Configure Build Path:**
   - Right-click project → Properties → Java Build Path
   - Set Modulepath to Java 11

4. **Install Plugins:**
   - Spring Tools 4
   - Lombok
   - Kubernetes Tools

#### VS Code Setup

1. **Install VS Code**

2. **Install Extensions:**
   ```bash
   code --install-extension vscjava.vscode-java-pack
   code --install-extension vscjava.vscode-spring-boot-dashboard
   code --install-extension ms-kubernetes-tools.vscode-kubernetes-tools
   code --install-extension ms-vscode.vscode-docker
   ```

3. **Open Project:**
   ```bash
   code tigateway
   ```

### Docker Configuration

#### Install Docker

**macOS:**
```bash
# Install Docker Desktop
brew install --cask docker
```

**Ubuntu:**
```bash
sudo apt update
sudo apt install docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

**Windows:**
1. Download Docker Desktop from [Docker](https://www.docker.com/products/docker-desktop)
2. Install and restart

#### Verify Docker Installation

```bash
docker --version
docker-compose --version
```

### Kubernetes Configuration

#### Install kubectl

**macOS:**
```bash
brew install kubectl
```

**Ubuntu:**
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

**Windows:**
```powershell
# Using Chocolatey
choco install kubernetes-cli

# Or download from GitHub releases
```

#### Install Minikube (for local development)

**macOS:**
```bash
brew install minikube
```

**Ubuntu:**
```bash
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
```

**Start Minikube:**
```bash
minikube start --driver=docker
minikube status
```

#### Install Helm

**macOS:**
```bash
brew install helm
```

**Ubuntu:**
```bash
curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
sudo apt-get install apt-transport-https --yes
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable.list
sudo apt-get update
sudo apt-get install helm
```

**Windows:**
```powershell
choco install kubernetes-helm
```

### Node.js Configuration (for Documentation)

#### Install Node.js

**macOS:**
```bash
brew install node
```

**Ubuntu:**
```bash
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt-get install -y nodejs
```

**Windows:**
1. Download Node.js from [nodejs.org](https://nodejs.org/)
2. Install using the installer

#### Install Yarn (optional)

```bash
npm install -g yarn
```

## Project Structure

```
tigateway/
├── docs/                          # Documentation
│   ├── docusaurus.config.js       # Docusaurus configuration
│   ├── sidebars.js               # Documentation sidebar
│   └── docs/                     # Documentation content
├── ti-gateway-core/              # Core gateway functionality
│   ├── src/main/java/
│   │   └── cn/tigateway/core/
│   │       ├── route/            # Route management
│   │       ├── filter/           # Filter implementations
│   │       ├── predicate/        # Predicate implementations
│   │       └── config/           # Configuration classes
│   └── pom.xml
├── ti-gateway-kubernetes/        # Kubernetes integration
│   ├── src/main/java/
│   │   └── cn/tigateway/kubernetes/
│   │       ├── discovery/        # Service discovery
│   │       ├── config/           # Kubernetes configuration
│   │       └── TiGatewayApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml       # Application configuration
│   │   └── application-dev.yml   # Development configuration
│   └── pom.xml
├── ti-gateway-admin/             # Admin interface
│   ├── src/main/java/
│   │   └── cn/tigateway/admin/
│   ├── src/main/resources/
│   └── pom.xml
├── helm/                         # Helm charts
│   └── tigateway/
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
├── docker/                       # Docker configurations
│   ├── Dockerfile
│   └── docker-compose.yml
├── scripts/                      # Build and deployment scripts
├── pom.xml                       # Root POM file
└── README.md
```

## Development Workflow

### 1. Branch Strategy

```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes and commit
git add .
git commit -m "feat: add new feature"

# Push branch
git push origin feature/new-feature

# Create pull request
```

### 2. Code Quality

#### Pre-commit Hooks

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Run code formatting
mvn spotless:apply

# Run tests
mvn test

# Check for TODO/FIXME comments
if grep -r "TODO\|FIXME" src/ --exclude-dir=target; then
    echo "Please resolve TODO/FIXME comments before committing"
    exit 1
fi
```

#### Code Formatting

```bash
# Format code using Spotless
mvn spotless:apply

# Check code formatting
mvn spotless:check
```

#### Static Analysis

```bash
# Run PMD analysis
mvn pmd:check

# Run SpotBugs analysis
mvn spotbugs:check

# Run Checkstyle
mvn checkstyle:check
```

### 3. Testing

#### Unit Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl ti-gateway-core

# Run tests with coverage
mvn test jacoco:report
```

#### Integration Tests

```bash
# Run integration tests
mvn verify -P integration-tests

# Run tests with Docker
mvn verify -P docker-tests
```

### 4. Building

#### Local Build

```bash
# Clean and compile
mvn clean compile

# Package application
mvn clean package

# Skip tests (for faster builds)
mvn clean package -DskipTests
```

#### Docker Build

```bash
# Build Docker image
docker build -t tigateway:latest .

# Build with specific tag
docker build -t tigateway:1.0.0 .
```

## Environment-Specific Configuration

### Development Environment

#### application-dev.yml

```yaml
# Development configuration
spring:
  profiles:
    active: dev
  logging:
    level:
      ti.gateway: DEBUG
      org.springframework.cloud.gateway: DEBUG
      org.springframework.web.reactive: DEBUG
      reactor.netty: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

tigateway:
  debug:
    enabled: true
    request-logging: true
    response-logging: true
    route-matching: true
```

#### Docker Compose for Development

```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  tigateway:
    build:
      context: .
      dockerfile: docker/Dockerfile.dev
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./logs:/app/logs
    depends_on:
      - redis
      - postgres

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: tigateway
      POSTGRES_USER: tigateway
      POSTGRES_PASSWORD: tigateway
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  redis_data:
  postgres_data:
```

### Testing Environment

#### application-test.yml

```yaml
# Testing configuration
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

tigateway:
  kubernetes:
    enabled: false
  cache:
    type: caffeine
```

### Production Environment

#### application-prod.yml

```yaml
# Production configuration
spring:
  profiles:
    active: prod
  logging:
    level:
      ti.gateway: INFO
      org.springframework.cloud.gateway: WARN
      org.springframework.web.reactive: WARN
      reactor.netty: WARN
    pattern:
      file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

tigateway:
  debug:
    enabled: false
    request-logging: false
    response-logging: false
```

## Common Issues and Solutions

### Issue 1: Java Version Mismatch

#### Problem
```
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.UnsupportedClassVersionError
```

#### Solution
```bash
# Check Java version
java -version
mvn -version

# Set correct JAVA_HOME
export JAVA_HOME=/path/to/java11
export PATH=$JAVA_HOME/bin:$PATH

# Rebuild project
mvn clean compile
```

### Issue 2: Maven Dependency Issues

#### Problem
```
Could not resolve dependencies for project
```

#### Solution
```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Update dependencies
mvn dependency:resolve

# Force update
mvn clean install -U
```

### Issue 3: Docker Build Issues

#### Problem
```
Docker build fails with permission denied
```

#### Solution
```bash
# Check Docker daemon
docker info

# Restart Docker service
sudo systemctl restart docker

# Check user permissions
sudo usermod -aG docker $USER
# Log out and log back in
```

### Issue 4: Kubernetes Connection Issues

#### Problem
```
Unable to connect to the server: dial tcp: lookup kubernetes
```

#### Solution
```bash
# Check kubectl configuration
kubectl config current-context
kubectl config get-contexts

# Test connection
kubectl cluster-info

# Reset configuration if needed
kubectl config use-context minikube
```

## Development Tools and Extensions

### IntelliJ IDEA Plugins

#### Essential Plugins

1. **Lombok Plugin**
   - Enables Lombok annotations
   - Install from JetBrains Marketplace

2. **Spring Boot Plugin**
   - Spring Boot support
   - Run configurations
   - Application properties support

3. **Kubernetes Plugin**
   - Kubernetes resource management
   - YAML validation
   - Resource templates

4. **Docker Plugin**
   - Docker file support
   - Container management
   - Docker Compose support

#### Recommended Settings

```json
// .idea/codeStyles/Project.xml
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <option name="name" value="java" />
          <option name="value" value="java.*" />
          <option name="name" value="javax" />
          <option name="value" value="javax.*" />
          <option name="name" value="org" />
          <option name="value" value="org.*" />
          <option name="name" value="com" />
          <option name="value" value="com.*" />
          <option name="name" value="cn" />
          <option name="value" value="cn.*" />
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

### VS Code Extensions

#### Java Development

```json
// .vscode/extensions.json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "vscjava.vscode-spring-boot-dashboard",
    "ms-kubernetes-tools.vscode-kubernetes-tools",
    "ms-vscode.vscode-docker",
    "gabrielbb.vscode-lombok",
    "redhat.vscode-yaml"
  ]
}
```

#### Settings

```json
// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "java.format.settings.url": ".vscode/java-formatter.xml",
  "spring-boot.ls.java.home": "/path/to/java11",
  "kubernetes.vs-kubernetes": {
    "vs-kubernetes.helm-path": "helm",
    "vs-kubernetes.kubectl-path": "kubectl"
  }
}
```

## Performance Optimization

### JVM Tuning

#### Development JVM Options

```bash
# Development JVM options
export JAVA_OPTS="-Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+PrintGC \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Dspring.profiles.active=dev"
```

#### Production JVM Options

```bash
# Production JVM options
export JAVA_OPTS="-Xms1g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -Dspring.profiles.active=prod"
```

### Maven Optimization

#### Maven Settings for Performance

```xml
<!-- ~/.m2/settings.xml -->
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
  
  <profiles>
    <profile>
      <id>performance</id>
      <properties>
        <maven.compiler.fork>true</maven.compiler.fork>
        <maven.compiler.maxmem>1024m</maven.compiler.maxmem>
        <maven.test.skip>false</maven.test.skip>
        <maven.javadoc.skip>true</maven.javadoc.skip>
        <maven.source.skip>true</maven.source.skip>
      </properties>
    </profile>
  </profiles>
</settings>
```

## Continuous Integration

### GitHub Actions

#### CI Pipeline

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests
      run: mvn clean test
    
    - name: Run integration tests
      run: mvn verify -P integration-tests
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
```

### Jenkins Pipeline

#### Jenkinsfile

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8.6'
        jdk 'JDK-11'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t tigateway:${BUILD_NUMBER} .'
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
```

## Troubleshooting

### Common Development Issues

#### 1. Port Already in Use

```bash
# Find process using port
lsof -i :8080
lsof -i :8081

# Kill process
kill -9 <PID>

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

#### 2. Memory Issues

```bash
# Check memory usage
free -h
top -p <java-pid>

# Increase heap size
export MAVEN_OPTS="-Xmx4g"
mvn clean compile
```

#### 3. Network Issues

```bash
# Check network connectivity
ping google.com
curl -I https://maven.aliyun.com

# Configure proxy if needed
export MAVEN_OPTS="-Dhttp.proxyHost=proxy.company.com -Dhttp.proxyPort=8080"
```

---

**Related Documentation**:
- [Coding Standards](./coding-standards.md)
- [Testing Guide](./testing.md)
- [Custom Components Development](./custom-components.md)
- [Debugging Guide](./debugging.md)
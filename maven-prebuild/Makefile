#!/usr/bin/make
SHELL=/bin/bash
RUN_APP_ARGS = --rm --user "$(shell id -u):$(shell id -g)"

IMAGE_NAME = royalwang/maven-prebuild-jdk11:spring-boot-2.6.3

.DEFAULT_GOAL := help
.PHONY: help

help: ## Show this help
	@printf "\033[33m%s:\033[0m\n" 'Available commands'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[32m%-14s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Docker image build
	docker build -t ${IMAGE_NAME} -f Dockerfile ../

push: ## Docker image push
	docker push ${IMAGE_NAME}

buildx: ## Docker image build multi-platform
	docker buildx build \
        --push \
        --platform=linux/amd64,linux/arm64 \
        --tag=${IMAGE_NAME} \
        -f Dockerfile ../

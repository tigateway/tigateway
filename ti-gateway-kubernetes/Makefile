#!/usr/bin/make
SHELL=/bin/bash

IMAGE_NAME = royalwang/gateway:1.0.0

.DEFAULT_GOAL := help
.PHONY: help

help: ## Show this help
	@printf "\033[33m%s:\033[0m\n" 'Available commands'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[32m%-14s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Docker build image
	docker build -t ${IMAGE_NAME} -f Dockerfile ../

push: ## Docker push image
	docker push ${IMAGE_NAME}

buildx: ## Docker image build multi-platform
	docker buildx build \
        --push \
        --platform=linux/amd64,linux/arm64 \
        --tag=${IMAGE_NAME} \
        -f Dockerfile ../

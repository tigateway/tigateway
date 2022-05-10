#!/usr/bin/make
SHELL=/bin/bash
RUN_APP_ARGS = --rm --user "$(shell id -u):$(shell id -g)"

.DEFAULT_GOAL := help
.PHONY: help

help: ## Show this help
	@printf "\033[33m%s:\033[0m\n" 'Available commands'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[32m%-14s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Docker build image
	docker build -t royalwang/gateway:1.0.0 -f Dockerfile ../

install: ## K8s helm install
	helm install gateway -n scg-k8s --create-namespace ./helm

upgrade: ## K8s helm upgrade
	helm upgrade gateway -n scg-k8s ./helm

uninstall: ## K8s helm uninstall
	helm uninstall gateway -n scg-k8s

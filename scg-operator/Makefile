#!/usr/bin/make
SHELL=/bin/bash
RUN_APP_ARGS = --rm --user "$(shell id -u):$(shell id -g)"

.DEFAULT_GOAL := help
.PHONY: help

help: ## Show this help
	@printf "\033[33m%s:\033[0m\n" 'Available commands'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[32m%-14s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Docker build image
	docker build -t royalwang/scg-operator:1.0.0 -f Dockerfile ../

push: ## Docker push image
	docker push royalwang/scg-operator:1.0.0

install: ## K8s helm install
	helm install scg-operator -n scg-k8s --create-namespace ./helm

upgrade: ## K8s helm upgrade
	helm upgrade scg-operator -n scg-k8s ./helm

uninstall: ## K8s helm uninstall
	helm uninstall scg-operator -n scg-k8s

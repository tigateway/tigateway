#!/usr/bin/make
SHELL=/bin/bash
HELM_CHART_PATH = "./tigateway"
CHART_NAME = "tigateway"
TIMEOUT = 2m
NAMESPACE = tigateway

.DEFAULT_GOAL := help
.PHONY: help

help: ## Show this help
	@printf "\033[33m%s:\033[0m\n" 'Available commands'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[32m%-14s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

install: ## K8s helm install
	helm upgrade --install --atomic --create-namespace \
          --namespace "${NAMESPACE}" \
          --timeout="${TIMEOUT}" \
          "${CHART_NAME}" ${HELM_CHART_PATH}

upgrade: ## K8s helm upgrade
	helm upgrade --install --atomic --create-namespace \
              --namespace "${NAMESPACE}" \
              --timeout="${TIMEOUT}" \
              "${CHART_NAME}" ${HELM_CHART_PATH}

uninstall: ## K8s helm uninstall
	helm uninstall "${CHART_NAME}" --namespace "${NAMESPACE}"

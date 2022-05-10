#!/bin/bash

set -eo pipefail

readonly SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly HELM_CHART_PATH="$SCRIPT_DIRECTORY/../helm"
readonly DEFAULT_NAMESPACE="spring-cloud-gateway"
readonly DEFAULT_TIMEOUT="2m"
readonly IMAGE_VALUES_FILE="${HELM_CHART_PATH}/scg-image-values.yaml"

helm_upgrade() {
  local chart_tarball_path chart_tarball chart_name namespace
  chart_tarball_path="$1"
  chart_tarball="$(basename "$chart_tarball_path")"
  chart_name="${chart_tarball%%-[[:digit:].]*}"
  namespace="$2"

  echo "chart tarball: $chart_tarball"
  echo "chart name: $chart_name"
  printf "%sWaiting up to %s for helm installation to complete%s\n" "$(tput setaf 4)" "${TIMEOUT}" "$(tput setaf 7)"

  if [[ "$#" -ge 4 ]]; then
    local operator_image gateway_image registry_credentials_secret
    operator_image="$3"
    gateway_image="$4"
    if [[ "$#" -eq 5 ]]; then
      registry_credentials_secret="$5"
    fi

    helm upgrade --install --atomic --create-namespace \
      --namespace "$namespace" \
      --set scg-operator.image="$operator_image",gateway.image="$gateway_image",scg-operator.registryCredentialsSecret="$registry_credentials_secret" \
      --timeout="${TIMEOUT}" \
      "$chart_name" "$chart_tarball_path" && check_status || error_diagnostic
  else
    helm upgrade --install --atomic --create-namespace \
      --namespace "$namespace" \
      --values "$IMAGE_VALUES_FILE" \
      --timeout="${TIMEOUT}" \
      "$chart_name" "$chart_tarball_path" && check_status || error_diagnostic
  fi
}

error_diagnostic(){
      printf "\n%sLogs from from operator pod:%s\n" "$(tput setaf 4)" "$(tput setaf 7)"
      kubectl logs -l app=scg-operator -n "${NAMESPACE}" || true
      printf "\n%sEvents from from installation namespace:%s\n" "$(tput setaf 4)" "$(tput setaf 7)"
      kubectl get events -n "${NAMESPACE}" --sort-by=.metadata.creationTimestamp
      printf "%sError installing Spring Cloud Gateway operator\n%s" "$(tput setaf 1)" "$(tput setaf 7)"
      troubleshooting_link
}

check_status(){
  printf "\n%sChecking Operator pod state %s\n" "$(tput setaf 4)" "$(tput setaf 7)"
  if kubectl rollout status deployment/scg-operator -n "${NAMESPACE}"; then
    echo "✔ Operator pods are running"
  else
    printf "%s✘ Unexpected status '$operatorPodState' for Operator pod %s\n" "$(tput setaf 1)" "$(tput setaf 7)"
    error_diagnostic
    exit 1
  fi
  printf "\n%sChecking custom resource definitions %s\n" "$(tput setaf 4)" "$(tput setaf 7)"
  declare -a crds=("springcloudgatewaymappings.tanzu.vmware.com" "springcloudgatewayrouteconfigs.tanzu.vmware.com" "springcloudgateways.tanzu.vmware.com")
  declare -a crd_created_at=()
  for crd in "${crds[@]}"; do
    local crdCreatedAt=$(kubectl get crd "$crd" -n "$NAMESPACE" -o=jsonpath='{.metadata.creationTimestamp}' --ignore-not-found)
    if [ -n "$crdCreatedAt" ]; then
      echo "✓ $crd successfully installed"
      if [[ "$OSTYPE" == 'darwin'* ]]; then
        crd_created_at+=($(date -jf '%Y-%m-%dT%H:%M:%SZ' "$crdCreatedAt" "+%s"))
      else
        crd_created_at+=($(date -d"$crdCreatedAt" +%s))
      fi
    else
      printf "%s✘ $crd could not be found %s\n" "$(tput setaf 1)" "$(tput setaf 7)"
    fi
  done

  if [[ ! ${#crd_created_at[@]} -eq ${#crds[@]} ]]; then
    printf "\n%sOne or more custom resource definition could not be found. The installation was not successful. %s\n" "$(tput setaf 1)" "$(tput setaf 7)"
    troubleshooting_link
    exit 1
  fi

  # find lower time
  firstCrdCreatedAtInSeconds=${crd_created_at[0]}
  for n in "${crd_created_at[@]}" ; do
      ((n < firstCrdCreatedAtInSeconds)) && firstCrdCreatedAtInSeconds=$n
  done
  local upperBound=$(( $firstCrdCreatedAtInSeconds + 10 ))

  for (( i = 1; i < ${#crd_created_at[@]}; i++ )); do
    local currentCrdCreatedAtInSeconds="${crd_created_at[i]}"
    if [[ $currentCrdCreatedAtInSeconds -lt $firstCrdCreatedAtInSeconds || $currentCrdCreatedAtInSeconds -gt $upperBound ]]; then
      printf "\n%s❌ The creation times for the spring cloud gateway custom resources seem to differ. They should be created around the same time. %s\n" "$(tput setaf 3)" "$(tput setaf 7)"
      kubectl get crd -n "$NAMESPACE"
      troubleshooting_link
      exit 1
    fi
  done

  printf "\n%sSuccessfully installed Spring Cloud Gateway operator\n%s" "$(tput setaf 2)" "$(tput setaf 7)"
}

troubleshooting_link() {
  printf "\nVisit the documentation at https://docs.pivotal.io/scg-k8s/troubleshooting.html for troubleshooting tips. \n"
}

help() {
  echo "Usage: '$0' or '$0 [--option value]'"
  echo "  When used without arguments, it is required to have run relocate-images.sh first."
  echo "  Available options:"
  printf "    %-30s %s\n" "--operator_image" "Full image name for SCG Operator. Use to skip use of relocate-images.sh."
  printf "    %-30s %s\n" "--gateway_image"  "Full image name for SCG Gateway. Use to skip use of relocate-images.sh."
  printf "    %-30s %s\n" "--registry_credentials_secret" "(Optional) Name for SCG Operator image pull secret."
  printf "    %-30s %s\n" "--namespace" "(Optional) Namespace to install the SCG Operator."
  printf "    %-30s %s\n" "-h | --help" "Prints this help."
}

backward_compatibility_validation() {
  if [[ $# -eq 1 && "$1" != "-h" && "$1" != "--help" ]]; then
    echo "Error: Passing single argument as namespace is no longer supported."
    echo
    help
    exit 1
  fi
}

main() {
  backward_compatibility_validation "$@"

  local OPERATOR_IMAGE GATEWAY_IMAGE
  local NAMESPACE="$DEFAULT_NAMESPACE"
  local TIMEOUT="$DEFAULT_TIMEOUT"
  while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
      --operator_image)
        OPERATOR_IMAGE="$2"
        shift
        shift
        ;;
      --gateway_image)
        GATEWAY_IMAGE="$2"
        shift
        shift
        ;;
      --registry_credentials_secret)
        REGISTRY_CREDENTIALS_SECRET="$2"
        shift
        shift
        ;;
      --namespace)
        NAMESPACE="$2"
        shift
        shift
        ;;
      --timeout)
        TIMEOUT="$2"
        shift
        shift
        ;;
      -h|--help)
        help
        exit 0
        ;;
      *)
        shift
        ;;
    esac
  done

  if [[ ! -f "$IMAGE_VALUES_FILE" && ( -z "$OPERATOR_IMAGE" || -z "$GATEWAY_IMAGE" ) ]]; then
    echo "$IMAGE_VALUES_FILE not found."
    echo "Run ./scripts/relocate-images.sh before installing spring cloud gateway, or use image name options."
    echo
    help
    exit 1
  fi

  if [[ -n $OPERATOR_IMAGE ]]; then
    helm_upgrade "$HELM_CHART_PATH"/spring-cloud-gateway-*.tgz "$NAMESPACE" "$OPERATOR_IMAGE" "$GATEWAY_IMAGE" "$REGISTRY_CREDENTIALS_SECRET"
  else
    helm_upgrade "$HELM_CHART_PATH"/spring-cloud-gateway-*.tgz "$NAMESPACE"
  fi
}

main "$@"

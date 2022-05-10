#!/usr/bin/env bash

set -euo pipefail

readonly SOURCE_REPOSITORY="dev.registry.tanzu.vmware.com/spring-cloud-gateway-for-kubernetes"
readonly DESTINATION_REPOSITORY="${1:?Destination repository must be set}"

readonly BASE_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly IMAGES_PATH="$BASE_DIRECTORY/images"
readonly HELM_CHART_PATH="$BASE_DIRECTORY/helm"

relocate_image() {
  local image_tarball="$1"
  local image_name version source_image destination_image

  if [[ ! "$image_tarball" =~ (.*)-([[:digit:].]{5,}.*)\.tar ]]; then
    echo "Unable to extract image information from $image_tarball"
    exit 1
  fi

  image_name=$(basename "${BASH_REMATCH[1]}")
  version=${BASH_REMATCH[2]}
  source_image="$SOURCE_REPOSITORY/$image_name:$version"
  destination_image="$DESTINATION_REPOSITORY/$image_name:$version"

  echo "Relocating image"
  echo "================"
  echo "image name: $image_name"
  echo "version: $version"
  echo "source repository: $source_image"
  echo "destination repository: $destination_image"

  docker load --input "$image_tarball"
  docker tag "$source_image" "$destination_image"
  docker push "$destination_image"

  cat << EOF >> "$HELM_CHART_PATH/scg-image-values.yaml"
${image_name}:
  image: "${destination_image}"
EOF
}

main() {
  rm -f "$HELM_CHART_PATH/scg-image-values.yaml"
  for image in "$IMAGES_PATH"/*.tar; do
    relocate_image "$image"
  done
}

main

#!/usr/bin/env bash

set -euo pipefail

api_base_url="${API_BASE_URL:-http://localhost:8000/api-docs}"
output_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/openapi"
services=(auth hr billing reception notification analytics)

mkdir -p "$output_dir"

for service in "${services[@]}"; do
  output_file="$output_dir/$service.json"
  echo "Fetching $service OpenAPI specification..."
  curl \
    --fail \
    --retry 3 \
    --retry-all-errors \
    --retry-delay 2 \
    --retry-connrefused \
    --connect-timeout 5 \
    --max-time 60 \
    --silent \
    --show-error \
    "$api_base_url/$service" \
    --output "$output_file"
  node -e "JSON.parse(require('fs').readFileSync(process.argv[1], 'utf8'))" "$output_file"
done

echo "Saved ${#services[@]} OpenAPI specifications to $output_dir."

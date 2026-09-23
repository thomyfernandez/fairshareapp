#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
if [ ! -f .env ]; then python3 scripts/configurar.py; fi
set -a
. ./.env
set +a
exec ./mvnw spring-boot:run "$@"

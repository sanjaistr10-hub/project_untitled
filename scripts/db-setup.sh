#!/usr/bin/env bash
# Creates the local dev + test databases on an already-running Postgres (no Docker needed).
# Usage: ./scripts/db-setup.sh            (connects as your OS user via localhost)
#        PGUSER=postgres ./scripts/db-setup.sh
# With Docker instead, just run: docker compose up -d   (it creates the same databases)
set -euo pipefail

DB_USER="${DB_USERNAME:-project_untitled}"
DB_PASS="${DB_PASSWORD:-project_untitled}"
PSQL="psql -h ${PGHOST:-localhost} -p ${PGPORT:-5432} -d postgres -v ON_ERROR_STOP=1 -Atq"

echo "Ensuring role '${DB_USER}' exists..."
$PSQL -c "DO \$\$ BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASS}';
  END IF;
END \$\$;"

for db in project_untitled project_untitled_test; do
  if [ "$($PSQL -c "SELECT 1 FROM pg_database WHERE datname = '${db}'")" != "1" ]; then
    echo "Creating database '${db}'..."
    $PSQL -c "CREATE DATABASE ${db} OWNER ${DB_USER}"
  else
    echo "Database '${db}' already exists."
  fi
done

echo "Done. Flyway creates the tables when the backend starts."

#!/bin/bash

set -e

echo "Starting replica initialization..."

if [ -s "$PGDATA/PG_VERSION" ]; then
    echo "Replica already initialized."
    exit 0
fi

echo "Waiting for primary..."

until pg_isready \
  -h db-primary \
  -p 5432 \
  -U root \
  -d cap_db; do

  echo "Primary not ready..."
  sleep 2

done

echo "Primary is ready."

echo "Performing pg_basebackup..."

export PGPASSWORD="replicator_password"

rm -rf "${PGDATA:?}"/*

pg_basebackup \
  -h db-primary \
  -p 5432 \
  -U replicator \
  -D "$PGDATA" \
  -Fp \
  -Xs \
  -P \
  -R

cat >> "$PGDATA/postgresql.auto.conf" <<EOF

# Replica
hot_standby = on
hot_standby_feedback = on
wal_receiver_timeout = 60s
wal_retrieve_retry_interval = 5s
EOF

chmod 700 "$PGDATA"

echo "Replica initialized successfully."
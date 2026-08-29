#!/bin/bash

set -e

echo "Waiting for primary..."

until pg_isready \
  -h db-primary \
  -p 5432 \
  -U root \
  -d cap_db
do
  sleep 2
done

echo "Primary is ready."

if [ -f "$PGDATA/standby.signal" ]; then
  echo "Replica already initialized."
  exit 0
fi

echo "Initializing replica..."

rm -rf "${PGDATA:?}"/*

export PGPASSWORD="replicator_password"

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

hot_standby = on
hot_standby_feedback = on
EOF

chmod 700 "$PGDATA"

echo "Replica initialized."
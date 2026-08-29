# Kubernetes Deployment - Using Standard PostgreSQL Image

## Overview

The k8s.yaml now uses the **standard `postgres:13` image** - no custom builds needed!

All replication configuration is done via environment variables.

---

## Key Configuration in k8s.yaml

### StatefulSet Environment Variables

```yaml
env:
  - name: POSTGRES_PASSWORD
    value: root
  - name: POSTGRES_USER
    value: root
  - name: POSTGRES_DB
    value: cap_db
  - name: POSTGRES_INITDB_ARGS
    value: -c wal_level=replica -c max_wal_senders=10 -c max_replication_slots=10 -c hot_standby=on -c hot_standby_feedback=on
```

These environment variables configure:
- `wal_level=replica`: Enable WAL for replication
- `max_wal_senders=10`: Allow 10 replication connections
- `max_replication_slots=10`: Support 10 replication slots
- `hot_standby=on`: Replica allows read queries
- `hot_standby_feedback=on`: Replica sends feedback to primary

---

## File Structure

```
cap-api/
├── docker-compose.yml              ✅ Local development (primary tool)
├── k8s/
│   └── k8s.yaml                    ✅ Kubernetes deployment
├── docker/
│   └── replica-init.sh             ✅ Auto-run on replica startup
└── src/main/resources/
    └── application.properties      ✅ Connects to postgres-primary
```

---

## No Additional Files Needed

**Removed:**
- ❌ `docker/Dockerfile.postgres` - Using standard postgres:13
- ❌ `docker/postgresql.conf` - Config via POSTGRES_INITDB_ARGS
- ❌ `docker/pg_hba.conf` - Default PostgreSQL settings

**Why?** Standard postgres:13 image already supports replication via environment variables!

---

## Deploying to Kubernetes

### 1. Simple Deployment

```bash
# Just apply the k8s.yaml - that's it!
kubectl apply -f k8s/k8s.yaml

# Monitor deployment
kubectl get statefulset postgres
kubectl get pods -l app=postgres
```

### 2. No Image Build Required

```bash
# No build step needed
# Just use: kubectl apply -f k8s/k8s.yaml
```

### 3. Verify Replication

```bash
kubectl port-forward postgres-0 5432:5432 &
psql -h localhost -U root -d cap_db -c "SELECT * FROM pg_stat_replication;"
```

---

## How replica-init.sh is Used

The `docker/replica-init.sh` script:

1. **Runs automatically** when replica pod starts (mounted as init script)
2. **Waits for primary** to be healthy
3. **Performs pg_basebackup** to copy all data
4. **Configures recovery** settings for streaming replication
5. **Enables hot standby** so replica accepts read queries

**No changes needed** - everything is automatic!

---

## Environment Variables Explained

| Variable | Value | Purpose |
|----------|-------|---------|
| `POSTGRES_PASSWORD` | root | Primary password |
| `POSTGRES_USER` | root | Admin user |
| `POSTGRES_DB` | cap_db | Default database |
| `POSTGRES_INITDB_ARGS` | (see below) | Replication configuration |

### POSTGRES_INITDB_ARGS Details

```
-c wal_level=replica              # Enable replication
-c max_wal_senders=10             # Allow multiple replicas
-c max_replication_slots=10       # Support multiple replication slots
-c hot_standby=on                 # Replica allows reads
-c hot_standby_feedback=on        # Replica sends feedback to primary
```

---

## Services in k8s.yaml

| Service | Purpose | Selector |
|---------|---------|----------|
| `postgres-primary` | Writes go here | Primary pod (postgres-0) |
| `postgres` | Headless service | All postgres pods |

Application connects to `postgres-primary:5432` for writes.

---

## Testing

### Test Replication

```bash
# Port forward to primary
kubectl port-forward postgres-0 5432:5432 &

# Create table
psql -h localhost -U root -d cap_db -c \
  "CREATE TABLE test (id SERIAL, data TEXT); INSERT INTO test VALUES (1, 'hello');"

# Port forward to replica
kubectl port-forward postgres-1 5432:5433 &

# Read from replica
psql -h localhost -U root -d cap_db -p 5433 -c "SELECT * FROM test;"
```

### Check Replication Status

```bash
kubectl exec postgres-0 -- psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"
```

---

## Troubleshooting

### Replica Not Connecting

```bash
# Check replica logs
kubectl logs postgres-1

# Verify primary is accessible
kubectl exec postgres-1 -- pg_isready -h postgres-primary

# Check replication status on primary
kubectl exec postgres-0 -- psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"
```

### High Replication Lag

```bash
# Check lag
kubectl exec postgres-0 -- psql -U root -d cap_db -c \
  "SELECT now() - pg_last_xact_replay_timestamp() AS lag;"

# If high, check network and disk I/O on both nodes
kubectl describe node
```

### Pods Stuck in CrashLoopBackOff

```bash
# Check pod logs
kubectl logs postgres-1 --previous

# Check events
kubectl describe pod postgres-1

# Check if replica initialization is failing
kubectl logs postgres-1 | grep "pg_basebackup\|error"
```

---

## Production Setup

For production, consider:

1. **Strong Passwords** - Change root/root
2. **SSL/TLS** - Add certificate configuration
3. **Synchronous Replication** - Uncomment in environment:
   ```
   -c synchronous_commit=remote_apply
   ```
4. **Separate Replication User** - Add dedicated user
5. **Backups** - Implement pg_basebackup strategy
6. **Monitoring** - Add Prometheus metrics

---

## Quick Reference

```bash
# Deploy
kubectl apply -f k8s/k8s.yaml

# Monitor
kubectl get statefulset postgres
kubectl get pods -l app=postgres

# Logs
kubectl logs postgres-0
kubectl logs postgres-1 -f

# Testing
kubectl port-forward postgres-0 5432:5432
psql -h localhost -U root -d cap_db

# Cleanup
kubectl delete -f k8s/k8s.yaml
```

---

**That's it! Simple, clean, and reliable replication with standard PostgreSQL image.** ✅

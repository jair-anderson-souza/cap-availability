# PostgreSQL Streaming Replication Setup Guide

## Overview

Your project now has **PostgreSQL Streaming Replication** configured for both Docker Compose and Kubernetes deployments.

### Architecture

```
┌─────────────────┐
│  Application    │
│   (cap-api)     │
└────────┬────────┘
         │
         ├──────────────────┐
         │                  │
    [WRITE]           [READ-ONLY]
         │                  │
    ┌────▼─────┐      ┌─────▼──────┐
    │ PRIMARY   │      │  REPLICA   │
    │  (5432)   │◄─────►  (5433)    │
    │ (Read     │ WAL  │ (Read)     │
    │  Write)   │Replication│       │
    └──────────┘      └────────────┘
         │                  │
         └──────────────────┘
       Streaming Replication
```

## Key Components

### Docker Compose Setup

#### Primary Database (db-primary)
- **Host:** db-primary
- **Port:** 5432
- **Role:** Master (Accepts reads and writes)
- **Configuration:** WAL level set to replica, accepts 10 replication connections
- **Data Volume:** db_primary_data

#### Replica Database (db-replica)
- **Host:** db-replica
- **Port:** 5433
- **Role:** Standby (Read-only, receives WAL stream)
- **Replication Method:** Streaming replication from primary
- **Data Volume:** db_replica_data

### Kubernetes Setup

Uses a **StatefulSet** with 2 replicas:
- **postgres-0:** Primary (Master)
- **postgres-1:** Replica (Standby)

Services:
- `postgres-primary`: ClusterIP service pointing to primary for writes
- `postgres`: Headless service for StatefulSet discovery
- `db`: Legacy service for backwards compatibility

## Features

### Consistency
✅ **ACID Guarantees:** PostgreSQL MVCC provides transaction consistency
✅ **Synchronous Replication Option:** Can be enabled for zero-loss failover
✅ **WAL Level Replica:** Ensures all changes are replicated

### Replication
✅ **Streaming Replication:** Replica receives WAL stream in real-time
✅ **Hot Standby:** Replica allows read-only queries
✅ **Automatic Recovery:** If primary fails, replica continues receiving changes

### Connection Strings

**For Application (Writes):**
```
Docker: jdbc:postgresql://db-primary:5432/cap_db
K8s: jdbc:postgresql://postgres-primary:5432/cap_db
```

**For Read-Only Replicas:**
```
Docker: jdbc:postgresql://db-replica:5433/cap_db
K8s: jdbc:postgresql://postgres-1.postgres:5432/cap_db
```

## Docker Compose Usage

### Starting Services
```bash
docker-compose up -d
```

### Checking Status
```bash
# Check primary
docker-compose exec db-primary pg_isready -U root

# Check replica
docker-compose exec db-replica pg_isready -U root

# View replication status
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT slot_name, slot_type, active FROM pg_replication_slots;"

# View replica lag
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT now() - pg_last_xact_replay_timestamp() AS replication_lag;"
```

### Testing Replication
```bash
# Create a test table on primary
docker-compose exec db-primary psql -U root -d cap_db -c \
  "CREATE TABLE test_replication (id SERIAL PRIMARY KEY, data TEXT);"

# Insert data
docker-compose exec db-primary psql -U root -d cap_db -c \
  "INSERT INTO test_replication (data) VALUES ('Test data');"

# Query replica (read-only)
docker-compose exec db-replica psql -U root -d cap_db -c \
  "SELECT * FROM test_replication;"
```

## Kubernetes Usage

### Deploying
```bash
kubectl apply -f k8s/k8s.yaml
```

### Checking Status
```bash
# View StatefulSet
kubectl get statefulset postgres

# View Pods
kubectl get pods -l app=postgres

# Check primary pod
kubectl logs postgres-0

# Check replica pod
kubectl logs postgres-1

# View replication status
kubectl exec postgres-0 -- psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"
```

### Port Forwarding for Testing
```bash
# Primary
kubectl port-forward postgres-0 5432:5432

# Replica
kubectl port-forward postgres-1 5433:5432
```

## Important Features Enabled

### postgresql.conf Settings (Kubernetes)
```
wal_level = replica
max_wal_senders = 10          # Max replication connections
max_replication_slots = 10    # Replication slots for multiple replicas
wal_keep_size = 1GB           # Keep WAL for replication
hot_standby = on              # Allow read queries on replica
hot_standby_feedback = on     # Feedback to primary about oldest query
```

### Docker Compose INITDB Args
```
POSTGRES_INITDB_ARGS=-c wal_level=replica -c max_wal_senders=10 -c max_replication_slots=10
```

## Handling Data Consistency

### Primary Writes
- All writes go to `db-primary` / `postgres-primary`
- Application must use primary connection for any modifications

### Replica Reads
- Use replica for read-only queries if needed
- Eventual consistency: Data appears on replica within milliseconds
- Replication lag visible via: `now() - pg_last_xact_replay_timestamp()`

### Transaction Consistency
- Use **SERIALIZABLE** isolation level for strong consistency
- Use **READ COMMITTED** for performance with eventual consistency

### Multi-Row Operations
```sql
-- Strong consistency (recommended for CAP API)
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;
-- Your operations here
COMMIT;
```

## Failover Scenarios

### If Primary Fails
1. Replica continues running as standby
2. Replica has all committed data from primary
3. Manual promotion: `kubectl exec postgres-1 -- pg_ctl promote`
4. Update connection string to replica
5. Restore original primary when ready

### If Replica Fails
1. Primary continues accepting writes
2. Start replica again
3. Replica automatically resumes replication from primary

### Synchronous Replication (Optional - For Zero-Loss Failover)
Modify postgresql.conf:
```conf
synchronous_commit = remote_apply
synchronous_standby_names = '*'
```
⚠️ Trade-off: Slower writes, but guaranteed replica has data

## Monitoring

### Key Metrics to Monitor

```sql
-- Replication lag
SELECT now() - pg_last_xact_replay_timestamp() AS replication_lag;

-- Replication slots
SELECT slot_name, active, restart_lsn FROM pg_replication_slots;

-- Connected replicas
SELECT client_addr, state, sync_state FROM pg_stat_replication;

-- Database size
SELECT pg_size_pretty(pg_database_size('cap_db'));
```

### Application-Level Consistency Checks
- Implement retry logic for connection failures
- Use connection pooling (HikariCP in Spring)
- Monitor replication lag in application metrics

## Troubleshooting

### Replica Not Receiving Data
```bash
# Check if replication is active
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"

# Check WAL location
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT pg_current_wal_lsn();"
```

### High Replication Lag
1. Check network latency between primary and replica
2. Monitor disk I/O on both servers
3. Consider enabling `hot_standby_feedback`
4. Increase `max_replication_slots` if needed

### Replica Falling Behind
```bash
# Increase max_wal_senders if needed
docker-compose exec db-primary psql -U root -d cap_db -c \
  "ALTER SYSTEM SET max_wal_senders = 20; SELECT pg_reload_conf();"
```

## Security Considerations

### Current Configuration
- ✅ Authentication with password (root/root)
- ⚠️ Not recommended for production
- ✅ Replication authentication via pg_hba.conf

### Production Recommendations
1. Use strong passwords
2. Use SSL/TLS for replication connections
3. Restrict IP access in pg_hba.conf
4. Use separate replication user
5. Enable audit logging
6. Regular backups with pg_basebackup

## Backing Up Replicated Database

```bash
# Backup from replica (non-blocking)
docker-compose exec db-replica pg_dump -U root cap_db > backup.sql

# Or use pg_basebackup
docker-compose exec db-primary pg_basebackup -D /backup -U root -v -P
```

## Next Steps

1. **Test Failover:** Stop primary, promote replica
2. **Monitor Lag:** Add replication lag to application metrics
3. **Load Testing:** Run Gatling simulations against replicated setup
4. **Production Hardening:** Implement security measures above

---

**Connection strings updated in:** `src/main/resources/application.properties`

# Quick Start: PostgreSQL Replication with Docker Compose

## Simplified Setup - Docker Compose Only

You now have a **simplified PostgreSQL replication setup** using only `docker-compose.yml` - no extra Dockerfile or config files needed!

### What Changed

- ✅ **docker-compose.yml**: Complete configuration for primary + replica
- ✅ **replica-init.sh**: Handles replica initialization automatically
- ✅ **No Dockerfile.postgres**: Using standard `postgres:13` image
- ✅ **No separate config files**: Replication settings in environment variables

---

## Getting Started (3 Commands)

### Step 1: Start Services

```bash
cd c:\Users\jair_\IdeaProjects\cap-api
docker-compose up -d
```

### Step 2: Wait for Health Checks

```bash
# Watch until both databases are healthy
docker-compose ps

# Shows when both db-primary and db-replica are "healthy"
```

### Step 3: Verify Replication Works

```bash
# Connect to primary
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"

# Should show: db-replica is connected and replicating
```

---

## Architecture

```
docker-compose.yml
├── api (cap-api:latest)
│   └── connects to: db-primary
├── db-primary (postgres:13)
│   ├── Port: 5432
│   ├── Read & Write
│   ├── WAL streaming enabled
│   └── Replication config in environment variables
└── db-replica (postgres:13)
    ├── Port: 5433
    ├── Read-only
    ├── Auto-initialized via replica-init.sh
    └── Receives WAL stream from primary
```

---

## How It Works

### docker-compose.yml Configuration

**Primary Database:**
```yaml
db-primary:
  image: postgres:13
  environment:
    - POSTGRES_INITDB_ARGS=-c wal_level=replica -c max_wal_senders=10 -c hot_standby=on
  volumes:
    - ./docker/replica-init.sh:/docker-entrypoint-initdb.d/replica-init.sh
```

**Replica Database:**
```yaml
db-replica:
  image: postgres:13
  volumes:
    - ./docker/replica-init.sh:/docker-entrypoint-initdb.d/replica-init.sh
  depends_on:
    db-primary:
      condition: service_healthy
```

### What replica-init.sh Does

Runs automatically when replica starts:
1. Waits for primary to be healthy
2. Performs `pg_basebackup` to copy all data
3. Configures recovery settings
4. Enables hot standby mode
5. Starts receiving WAL stream

**You don't need to run it manually** - Docker handles everything!

---

## Common Tasks

### Connect to Primary (Read/Write)

```bash
docker-compose exec db-primary psql -U root -d cap_db
```

### Connect to Replica (Read-Only)

```bash
docker-compose exec db-replica psql -U root -d cap_db
```

### Check Replication Status

```bash
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"
```

### Check Replication Lag

```bash
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT now() - pg_last_xact_replay_timestamp() AS lag;"
```

### Test Data Replication

```bash
# Create on primary
docker-compose exec db-primary psql -U root -d cap_db -c \
  "CREATE TABLE test (id SERIAL, data TEXT); INSERT INTO test VALUES (1, 'test');"

# Verify on replica
docker-compose exec db-replica psql -U root -d cap_db -c \
  "SELECT * FROM test;"
```

### View Logs

```bash
# Primary logs
docker-compose logs -f db-primary

# Replica logs
docker-compose logs -f db-replica

# Application logs
docker-compose logs -f api
```

### Stop Everything

```bash
docker-compose down

# Keep data
# docker-compose down  (volumes remain)

# Remove everything including data
# docker-compose down -v
```

---

## File Structure

```
cap-api/
├── docker-compose.yml              ✅ Complete setup (all you need!)
├── docker/
│   └── replica-init.sh             ✅ Auto-run on replica start
├── src/main/resources/
│   └── application.properties      ✅ Points to db-primary
└── k8s/
    └── k8s.yaml                    📖 For Kubernetes deployments
```

---

## Troubleshooting

**Problem: Replica fails to start**
```bash
# Check replica logs
docker-compose logs db-replica

# Common issue: primary not ready
# Solution: Restart and wait for primary health check
docker-compose restart db-primary db-replica
```

**Problem: Cannot connect to database**
```bash
# Check services are running
docker-compose ps

# Check network
docker-compose exec api ping db-primary

# Verify credentials
docker-compose exec db-primary psql -U root -d cap_db -c "SELECT 1;"
```

**Problem: Data not replicating**
```bash
# Check if replication is running
docker-compose exec db-primary psql -U root -d cap_db -c \
  "SELECT * FROM pg_stat_replication;"

# If empty, restart replica
docker-compose restart db-replica
```

---

## Production Considerations

For production deployment:
1. Use strong passwords (update root/root)
2. Enable SSL for connections
3. Set `synchronous_commit = remote_apply` for zero-loss failover
4. Monitor replication lag
5. Plan failover procedure
6. Regular backups with pg_basebackup

---

## Next Steps

1. ✅ Run `docker-compose up -d`
2. ✅ Test replication with sample queries
3. ✅ Run your Gatling load tests against the replicated database
4. ✅ Monitor replication lag during tests

**That's it! You now have a fully replicated PostgreSQL setup.** 🚀

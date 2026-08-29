# Minikube / Kubernetes Deployment Guide

## Overview

This guide covers deploying to Kubernetes (Minikube for local development).

**For Docker Compose (development)**, see [QUICKSTART.md](QUICKSTART.md) - that's the fastest way to get started!

---

## Files Structure

```
cap-api/
├── docker-compose.yml              ✅ Local development (start here)
├── k8s/
│   └── k8s.yaml                    ← Kubernetes deployment
├── docker/
│   └── replica-init.sh             ← Auto-run on replica startup
└── src/main/resources/
    └── application.properties
```

---

## Kubernetes Setup

The `k8s.yaml` uses:
- **Standard postgres:13 image** (no custom build needed)
- **StatefulSet** with 2 replicas (primary + replica)
- **Services** for application connectivity
- **Environment variables** for replication configuration

### Key Components in k8s.yaml

1. **StatefulSet**: `postgres` (2 replicas)
   - `postgres-0`: Primary (reads + writes)
   - `postgres-1`: Replica (read-only, auto-synced)

2. **Services**:
   - `postgres-primary`: Points to primary for writes
   - `postgres`: Headless service for discovery

3. **Config**: Replication via `POSTGRES_INITDB_ARGS` environment variable
   ```
   -c wal_level=replica -c max_wal_senders=10 -c hot_standby=on
   ```

---

## Deploying to Minikube

### Step 1: Start Minikube

```bash
minikube start

# Verify it's running
minikube status
```

### Step 2: Deploy Application & Database

```bash
cd c:\Users\jair_\IdeaProjects\cap-api

# Build application image (if needed)
docker build -t cap-api:v1 .

# Deploy to Minikube
kubectl apply -f k8s/k8s.yaml

# Watch deployment
kubectl get pods -l app=postgres -w
kubectl get pods -l app=cap-api -w
```

### Step 3: Verify Deployment

```bash
# Check all resources
kubectl get all

# Check StatefulSet
kubectl get statefulset postgres

# Check pods
kubectl get pods -l app=postgres
kubectl get pods -l app=cap-api

# View logs
kubectl logs postgres-0
kubectl logs postgres-1
kubectl logs -l app=cap-api
```

---

## Port Forwarding & Testing

### Connect to Primary Database

```bash
# Forward local port 5432 to postgres-0
kubectl port-forward postgres-0 5432:5432 &

# Connect with psql
psql -h localhost -U root -d cap_db

# In psql:
SELECT * FROM pg_stat_replication;  -- Should show replica connected
```

### Connect to Replica Database

```bash
# Forward to replica
kubectl port-forward postgres-1 5432:5433 &

# Connect (read-only)
psql -h localhost -U root -d cap_db -p 5433
```

### Connect to Application

```bash
# Forward application port
kubectl port-forward service/cap-api 8080:8080 &

# Access at http://localhost:8080
```

---

## Replica Initialization

The `docker/replica-init.sh` script:
- Runs automatically when replica pod starts
- Performs `pg_basebackup` from primary
- Configures streaming replication
- Enables hot standby mode

**No manual setup needed** - Kubernetes handles it!

---

## Troubleshooting Kubernetes

### Pods Stuck in Pending

```bash
# Check node resources
kubectl describe node

# Check pod events
kubectl describe pod postgres-1

# If resource-constrained, reduce requests in k8s.yaml
```

### Replication Not Starting

```bash
# Check replica logs
kubectl logs postgres-1

# Check if primary is accessible
kubectl exec postgres-1 -- pg_isready -h postgres-primary

# Restart replica
kubectl delete pod postgres-1  # StatefulSet will recreate
```

### Persistent Volume Issues

```bash
# Check PVCs
kubectl get pvc

# Check PV status
kubectl get pv

# Inspect specific PVC
kubectl describe pvc postgres-storage-postgres-0
```

### Network Issues

```bash
# Test pod-to-pod connectivity
kubectl exec postgres-0 -- ping postgres-1.postgres

# Test DNS resolution
kubectl exec postgres-0 -- nslookup postgres-primary
```

---

## Cleanup

### Remove Deployment

```bash
# Delete Kubernetes resources
kubectl delete -f k8s/k8s.yaml

# Delete persistent volumes (keep data)
kubectl delete pvc -l app=postgres

# Delete everything including data
kubectl delete pvc,pv -l app=postgres
```

### Stop Minikube

```bash
# Stop but keep data
minikube stop

# Delete Minikube entirely
minikube delete
```

---

## Production Considerations

For production Kubernetes:

1. **Use StatefulSet** ✅ (already configured)
2. **Storage**: Use production-grade persistent volumes
3. **Security**:
   - Use Secrets for passwords (not environment variables)
   - Enable Pod Security Policies
   - Use network policies
4. **High Availability**:
   - Consider Patroni for automatic failover
   - Use multiple availability zones
   - Implement backup strategy
5. **Monitoring**:
   - Add Prometheus metrics
   - Monitor replication lag
   - Alert on pod failures
6. **Networking**:
   - Use Ingress for application access
   - Enable TLS for database connections

---

## Quick Commands Reference

```bash
# Deployment
kubectl apply -f k8s/k8s.yaml
kubectl get statefulset postgres
kubectl get pods -l app=postgres

# Logs
kubectl logs postgres-0
kubectl logs postgres-1 -f
kubectl logs -l app=cap-api

# Debugging
kubectl describe pod postgres-0
kubectl exec postgres-0 -- psql -U root -d cap_db -c "SELECT 1;"
kubectl port-forward postgres-0 5432:5432

# Cleanup
kubectl delete -f k8s/k8s.yaml
kubectl delete pvc -l app=postgres

# Minikube
minikube start
minikube stop
minikube delete
minikube dashboard
```

---

## Next Steps

1. Start with [QUICKSTART.md](QUICKSTART.md) for local development with docker-compose
2. Once tested locally, deploy to Minikube using this guide
3. Test failover and recovery scenarios
4. Plan production deployment strategy

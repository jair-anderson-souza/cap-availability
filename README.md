# Gatling Application

## Getting Started

## Versions

`openjdk:14.0.2`
`gradle:6.8.3`

## Follow next steps to a local deploy:


## In root foler, run app/
`./gradlew clean bootRun`
<br><br>


# Set total execution time:
![Execution time](https://github.com/jass2125/gatling-application/blob/master/img/execution-time.png)
<br><br>

# Run stress tests:
`./gradlew clean gatlingRun`
<br><br>

# Go to folder:
`./build/reports/gatling/../index.html`
<br><br>

# Reports:
![Reports](https://github.com/jass2125/gatling-application/blob/master/img/graphics.png)


# Kubernetes
#### 1. Build image
`docker build -t cap-app:v1 .`

#### 2. Create cluster
`minikube start --profile cap-app`

#### 3.Upload image
`minikube image load cap-app:v1 --profile cap-app`

## 4. Set Context
`kubectl cluster-info --context kind-cap-app`
kubectl cluster-info --context cap-app

## 5. Apply
`kubectl apply -f cap.yaml`

## 6. List all pods
`kubectl get pods`

## 6. List all hpa
`kubectl get pods -w`

## 7. List all deployments
`kubectl get deploy`

## 8. List all svc
`kubectl get svc`

## 9. List all replicasets
`kubectl get rs`

## 10. Create replicaset by a file
`kubectl apply -f rs.yaml`

## 11. Port Forward
`kubectl port-forward pod/nginx 8000:80`

## 12. Delete pods
`kubectl delete pod nginx`

## 13. List images in control pane
`docker exec -it cap-app-control-plane crictl images`


kubectl delete replicaset,service,deployment,pod --all
minikube addons enable metrics-server --profile cap-app
http://localhost:8080/person/1
kubectl exec -it db-59b9bc6688-lpkch -- /bin/bash
psql -d cap_db
minikube tunnel --profile cap-app`
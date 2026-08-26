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
`docker build -t cap-availability-app:v1 .`

#### 2. Create cluster
`minikube start --profile cap-availability`

#### 3.Upload image
`minikube image load cap-availability-app:v1 --profile cap-availability`

minikube tunnel --profile cap-availability`

## Set Context
`kubectl cluster-info --context kind-cap-availability`
kubectl cluster-info --context cap-availability

## Apply
`kubectl apply -f service.yaml`

## List all pods
`kubectl get pods`

## List all deployments
`kubectl get deploy`

## List all svc
`kubectl get svc`

## List all replicasets
`kubectl get rs`

## Create replicaset by a file
`kubectl apply -f rs.yaml`

## Port Forward
`kubectl port-forward pod/nginx 8000:80`

## Delete pods
`kubectl delete pod nginx`

## List images in control pane
`docker exec -it cap-availability-control-plane crictl images`



kubectl delete replicaset,service,deployment,pod --all


minikube addons enable metrics-server --profile cap-availability

http://localhost:8080/person/1

kubectl exec -it db-59b9bc6688-lpkch -- /bin/bash

psql -d capavailability_db
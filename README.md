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


Create cluster
docker build -t gatling-app:v1 .

kubectl apply -f deployment.yaml
kind-windows-amd64.exe load docker-image gatling-app:v1 --name fullcycle
kind-windows-amd64.exe load docker-image gatling-app:v2 --name fullcycle
docker exec -it fullcycle-control-plane crictl images
kind-windows-amd64.exe load docker-image gatling-app:v1 --name fullcycle
kind-windows-amd64.exe create cluster --name fullcycle




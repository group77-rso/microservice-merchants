# RSO: Template microservice

## Prerequisites

```bash
docker run -d --name pg-merchants -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=merchants -p 5432:5432 postgres:13
```

## Build and run commands
```bash
mvn clean package
cd api/target
java -jar merchants-api-1.0.0-SNAPSHOT.jar
```
Available at: localhost:8081/v1/merchants

## Run in IntelliJ IDEA
Add new Run configuration and select the Application type. In the next step, select the module api and for the main class com.kumuluz.ee.EeApplication.

Available at: localhost:8081/v1/merchants

## Docker commands
```bash
docker build -t template-image .   
docker images
docker run template-image    
docker tag template-image barbaralipnik/merchants:latest  
docker push barbaralipnik/merchants
docker ps
```

```bash
docker network ls  
docker network rm rso
docker network create rso
docker run -d --name pg-merchants -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=merchants -p 5432:5432 --network rso postgres:13
docker inspect pg-merchants
docker run -p 8081:8081 --network rso -e KUMULUZEE_DATASOURCES0_CONNECTIONURL=jdbc:postgresql://pg-merchants:5432/merchants barbaralipnik/merchants:latest
```

## Consul
```bash
consul agent -dev
```
Available at: localhost:8500

Key: environments/dev/services/merchants-service/1.0.0/config/rest-properties/maintenance-mode

Value: true or false

## Kubernetes
```bash
kubectl version
kubectl --help
kubectl get nodes
kubectl create -f k8s/merchants-deployment.yaml 
kubectl apply -f k8s/merchants-deployment.yaml 
kubectl get services 
kubectl get deployments
kubectl get pods
kubectl logs merchants-deployment-6f59c5d96c-rjz46
kubectl delete pod merchants-deployment-6f59c5d96c-rjz46
```
Secrets: https://kubernetes.io/docs/concepts/configuration/secret/


# RSO: Template microservice

## Prerequisites

```bash
docker run -d --name pg-products -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=products -p 5433:5432 postgres:13
```

## Build and run commands
```bash
mvn clean package
cd api/target
java -jar products-api-1.0.0-SNAPSHOT.jar
```
Available at: localhost:8080/v1/products

## Run in IntelliJ IDEA
Add new Run configuration and select the Application type. In the next step, select the module api and for the main class com.kumuluz.ee.EeApplication.

Available at: localhost:8080/v1/products

## Docker commands
```bash
docker build -t template-image .   
docker images
docker run template-image    
docker tag template-image onlygregor/template-image   
docker push onlygregor/template-image
docker ps
```

```bash
docker network ls  
docker network rm rso
docker network create rso
docker run -d --name pg-products -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=products -p 5432:5432 --network rso postgres:13
docker inspect pg-products
docker run -p 8080:8080 --network rso -e KUMULUZEE_DATASOURCES0_CONNECTIONURL=jdbc:postgresql://pg-products:5432/products onlygregor/template-image:2022-11-14-12-45-13
```

## Consul
```bash
consul agent -dev
```
Available at: localhost:8500

Key: environments/dev/services/products-service/1.0.0/config/rest-properties/maintenance-mode

Value: true or false

## Kubernetes
```bash
kubectl version
kubectl --help
kubectl get nodes
kubectl create -f products-deployment.yaml 
kubectl apply -f products-deployment.yaml 
kubectl get services 
kubectl get deployments
kubectl get pods
kubectl logs products-deployment-6f59c5d96c-rjz46
kubectl delete pod products-deployment-6f59c5d96c-rjz46
```
Secrets: https://kubernetes.io/docs/concepts/configuration/secret/


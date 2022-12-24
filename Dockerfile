FROM amazoncorretto:18
RUN mkdir /app

WORKDIR /app

ADD ./api/target/merchants-api-1.0.0-SNAPSHOT.jar /app

EXPOSE 8081

CMD ["java", "-jar", "merchants-api-1.0.0-SNAPSHOT.jar"]
#ENTRYPOINT ["java", "-jar", "products-api-1.0.0-SNAPSHOT.jar"]
#CMD java -jar products-api-1.0.0-SNAPSHOT.jar

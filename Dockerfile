FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/java-graphql-users.jar /app/java-graphql-users.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/java-graphql-users.jar"]

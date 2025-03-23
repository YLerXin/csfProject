# 1) BUILD ANGULAR FRONTEND
FROM node:23 AS ng-build

WORKDIR /src/app
RUN npm install -g @angular/cli

COPY client/package*.json ./

RUN npm ci

RUN npm install @angular/material @angular/cdk dexie @ngrx/store @stomp/stompjs

COPY client/ ./

RUN ng build --configuration production

FROM openjdk:23-jdk AS j-build

WORKDIR /build

COPY Server/.mvn .mvn
COPY Server/mvnw .
COPY Server/pom.xml .

COPY Server/src src

COPY --from=ng-build /src/app/dist/project/browser/ src/main/resources/static/

RUN chmod +x mvnw && ./mvnw package -DskipTests

# 3) FINAL RUNTIME IMAGE
FROM openjdk:23-jdk
WORKDIR /app

COPY --from=j-build /build/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# ENTRYPOINT
ENTRYPOINT ["java", "-jar", "app.jar"]

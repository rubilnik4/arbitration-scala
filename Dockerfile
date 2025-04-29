FROM openjdk:17-jdk-slim

WORKDIR /app

COPY src/main/scala/arbitration /app

EXPOSE 8080
EXPOSE 5005

CMD ["sbt", "run"]
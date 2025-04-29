FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /app

COPY build.sbt .
COPY project/ ./project/

RUN sbt update

COPY . .

EXPOSE 8080
EXPOSE 5005

CMD ["sbt", "-jvm-debug", "5005", "run"]
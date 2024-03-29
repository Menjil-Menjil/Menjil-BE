FROM ubuntu:latest
FROM openjdk:17
ARG DEBIAN_FRONTEND=noninteractive
ARG JAR_FILE=build/libs/*.jar
#RUN apt-get install -y tzdata
#RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]

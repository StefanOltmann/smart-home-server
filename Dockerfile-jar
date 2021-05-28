# syntax=docker/dockerfile:1
FROM amazoncorretto:11-alpine
MAINTAINER Stefan Oltmann

WORKDIR /server

# The application
COPY smart-home-server.jar .

# REST interface
EXPOSE 50000

# KNX Control Channel Port
EXPOSE 50011

# KNX Data Channel Port
EXPOSE 50012

# Config files and logs go here
VOLUME /server/data

# Run the application
CMD ["java", "-jar", "/server/smart-home-server.jar"]

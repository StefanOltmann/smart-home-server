# syntax=docker/dockerfile:1
FROM debian:stable-slim
MAINTAINER Stefan Oltmann

WORKDIR /server

# The application
COPY smart-home-server .

# Keystore for HTTPS connections
COPY keystore.jks .

# REST interface
EXPOSE 50000

# KNX Control Channel Port
EXPOSE 50011

# KNX Data Channel Port
EXPOSE 50012

# Config files and logs go here
VOLUME /server/data

# Run the application
CMD ["./smart-home-server"]

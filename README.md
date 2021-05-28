# Stefans Smart Home Server

[![CI](https://github.com/StefanOltmann/smart-home-server/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/StefanOltmann/smart-home-server/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=smart-home-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=smart-home-server)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This is a REST API to control a KNX based smart home.

## Motivation

I'm a software developer who owns a KNX-based smart home.

Before I used **[openHAB](https://www.openhab.org/)**, but that was not as reliable and lightweight as I want it to be.

Looking for a nice spare time project where I can code some Kotlin and play around with the Alexa SDK, Jetpack Compose
and Vaadin I decided to start my own Smart Home project at a much smaller scale.

The goal is to keep everything as simple and lightweight as possible.

## Prerequisites

To run the server you need a KNX NET/IP device. I tested it with my **GIRA X1**, but something simpler should also work.
Also you should have a server like an **Raspberry Pi** or an **Intel NUC** that you are comfortable running 24/7.

If don't want to use the Alexa Skill that's all you need. Otherwise you must be willing to open and redirect a port in
your firewall.

## Status

Supported device types:

- (light) switches
- dimmers / rollershutters
- heatings

Supported device actions:

- setting power state (ON/OFF)
- setting percentage (0..100%)
- setting target temperature (°C)

## Used frameworks

This project uses Quarkus, the Supersonic Subatomic Java Framework. If you want to learn more about Quarkus, please
visit its website: https://quarkus.io/ .

The KNX backend used is [knx-core](https://github.com/pitschr/knx-core).

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

## Packaging the application on your own

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `smart-home-server-XY-runner.jar` file in the `/build` directory.

Be aware that it’s not an _uber-jar_ as the dependencies are copied into the `build/lib` directory.

If you want to build an _uber-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar build/smart-home-server-XY-runner.jar`.

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/smart-home-server-XY-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Optional: Packaging the application using GitHub Actions

This project has GitHub Actions configured to build an _uber-jar_ file as well as native binaries for Windows, Linux and
MacOS.

Using this free feature is the easiest way to get an binary file as you don't need to install anything on your computer.

Just navigate to the [Actions page](https://github.com/StefanOltmann/smart-home-server/actions), click on the latest run
and grab the artifacts.

## Using the Linux binary

Checkout the [releases page](https://github.com/StefanOltmann/smart-home-server/releases/) for the latest artifacts.

The [keystore.jks](src/main/resources/keystore.jks) for HTTPS connections. You can use mine or generate your own.
This file needs to be put aside the executable.

The [devices.json](docs/devices.json) for configuration of your KNX devices data points must be placed in a subdirectory
named `data`.

Make the binary executable with `chmod +x smart-home-server`.

Run it with `./smart-home-server`.

During the first start the server will create an _auth_code.txt_ (also in `data`) that contains a security token for
requests to the service.

The service will then be reachable on [https://localhost:50000/](https://localhost:50000/).

Now you can send a `GET` request to `https://localhost:50000/devices/` with the header key `AUTH_CODE` set to the
security token (see `auth_code.txt`). If this returns you something that looks like the provided _devices.json_ the
service is running.

If you have installed `curl` run the following command to test the service:\
`curl -k -H "AUTH_CODE: $(cat data/auth_code.txt)" https://localhost:50000/devices/current-states`

The URL `https://localhost:50000/devices/current-states` should return something like this:

```
[{
  "deviceId": "kitchen_light"
  "powerState": "OFF",
}, {
  "deviceId": "living_room_blinds"
  "powerState": "ON",
  "percentage": 100,
}, {
  "deviceId": "living_room_heating"
  "currentTemperature": 21.7,
  "targetTemperature": 21.0,
}]
```

In this example you can turn the kitchen light on by
calling `https://localhost:50000/device/kitchen_light/set/power-state/value/ON`
or set a percentage to the living room blinds by
calling `https://localhost:50000/device/living_room_blinds/set/percentage/value/50`.

If all of that works you should consider using a DynDNS service and redirect a port to make the service available as
something like `https://home.mydomain.com:50000` so you can use this API with
the [Alexa Skill](https://github.com/StefanOltmann/smart-home-alexaskill) or the 
[Android App](https://github.com/StefanOltmann/smart-home-android).

## Optional: Using the Docker image

There is also a docker image available: https://hub.docker.com/repository/docker/stefanoltmann/smart-home-server

You should mount the `/server/data` directory to a volume.

The _auth_code.txt_ and logs will be located there.
Also you need to put the [devices.json](docs/devices.json) configuration file here.

**IMPORTANT: Networking must be set to "host" mode to make it work.**

## Optional: Connection to InfluxDB

Per default the service will write a `device_state_history.csv` that you can import in a tool of your choice.

But you also can store measurements in a [InfluxDB](https://github.com/influxdata/influxdb) directly if you want to create charts with [Grafana](https://github.com/grafana/grafana).

To configure this you need to place a [influxdb.ini](docs/influxdb.ini) in your `data` directory.

The first line should be the URL and the second line the token.
The organisation must be named `smarthome` and the bucket must be named `SmartHome`.

**NOTE: Since Retrofit does not work with GraalVM native-image this feature is not supported for the binary distribution.**

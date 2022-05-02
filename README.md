
# Spring Boot Cloud Gateway

[![Java CI with Maven](https://github.com/gcalsolaro/spring-cloud-gateway/actions/workflows/maven.yml/badge.svg)](https://github.com/gcalsolaro/spring-cloud-gateway/actions/workflows/maven.yml)
> **Sample Gateway Application powered by Spring Cloud 2021.0.1**


## Table of Contents

   * [Spring Boot Cloud Gateway](#spring-boot-cloud-gateway)
      * [Architecture](#architecture)
      * [Prerequisites](#prerequisites)
      * [Use Case](#use-case)
      * [Swagger UI](#swagger-ui)
      

## Architecture

The technology stack used is provided by Spring Cloud:

* **_Spring Boot_** - 2.6.6
* **_Spring Cloud_** - 2021.0.1
* **_Spring Cloud Resilience4j_** - 2.1.1
* **_Springdoc Openapi Webflux UI_** - 1.6.7
* **_Lombok_** - 1.18.22
* **_auth0_** - 3.19.1

## Prerequisites
* **_JDK 8_** - Install JDK 1.8 version
* **_Maven_** - Download latest version
* **_Listening example microservices on port 8081_** [@see - application.yml](https://github.com/gcalsolaro/spring-cloud-gateway/blob/main/src/main/resources/application.yml)

## Use Case

 * **Modify the Open Api** of a microservice behind the gateway adding Authentication or infer custom server

- **Track** requests and responses

- **Modify input and output** data through Filters

- Authenticate calls via **JWT** and **Reactive Authentication**

- Use various pattern as **CircuitBreaker, TimeLimiter, RateLimiter and Retry** provided by **resilience4j**

## Swagger UI

Method | Endpoint |
--- | --- |
`GET` | *http://localhost:8080/swagger-ui.html* |


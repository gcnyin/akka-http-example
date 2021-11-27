# akka-http-example

Inspired by [Techpower benchmarks](https://www.techempower.com/benchmarks).

Powered by Akka, Tapir and MongoDB.

## Requirements

- Java 8+
- MongoDB

## Preparation

Install MongoDB.

```
docker-compose up -d
```

Run the scripts in the `mongo-migration` directory in the MongoDB console.

## Build

```
sbt clean stage
```

## Run

```
./target/universal/stage/bin/akka-http-example
```

## Swagger UI

<http://localhost:8080/docs/index.html?url=/docs/docs.yaml>

# akka-http-performance-test-demo

Inspired by [Techpower benchmarks](https://www.techempower.com/benchmarks).

## Preparation

Install MongoDB.

```
docker-compose up -d
```

Run the scripts in the `mongo-migration` directory in the MongoDB console.

## Build

```
sbt stage
```

## Run

```
./target/universal/stage/bin/akka-http-performance-test-demo
```

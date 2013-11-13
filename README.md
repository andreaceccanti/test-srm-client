# test-srm-client

A simple Java SRM client to be used in StorM load tests.

## Build time dependencies

- maven 3

## Build instructions

Type:

```bash
mvn package
```

This will generate an uber jar containing the srm client, voms libraries,
canl and axis stuff name test-srm-client-VERSION.jar.

## Runtime dependencies

- bcprov-jdk16
- bcmail-jdk16

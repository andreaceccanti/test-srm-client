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

## Run srmPing example

In order to run a simple srmPing against a valid StoRM SRM endpoint (e.g. *https://omii006-vm03.cnaf.infn.it:8444/*), 
create your own VOMS proxy and then run:

```bash
git clone https://github.com/italiangrid/test-srm-client.git
cd test-srm-client
sh run-example-test.sh https://omii006-vm03.cnaf.infn.it:8444
```

At the end of the output you should see the srmPing output, like:

```bash
SRM Version: v2.2
OtherInfo: 
backend_type = StoRM
backend_version = <FE:1.8.7><BE:1.11.7>
backend_os_distribution = Scientific Linux release 6.6 (Carbon)
```



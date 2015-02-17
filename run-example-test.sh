#!/bin/sh

CP="target/test-srm-client-0.0.1-SNAPSHOT.jar:/usr/share/java/bcprov.jar:/usr/share/java/bcmail.jar"
JVM_OPTS="-Djavax.net.debug=ssl:handshake"
MAINCLASS="org.italiangrid.srm.client.TestSRMPing"

java $JVM_OPTS -cp $CP $MAINCLASS $1

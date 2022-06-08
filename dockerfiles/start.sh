#!/bin/bash

# start service
cd /workspace
JAR_FILE=`ls | grep .jar$`
$JAVA_HOME/bin/java -jar -Dspring.profiles.active=$env $JAR_FILE

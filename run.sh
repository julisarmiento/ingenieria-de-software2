#!/bin/bash


PID=$(lsof -t -i:8080)
if [ -n "$PID" ]; then
    echo "Puerto 8080 ocupado por PID $PID, matando..."
    kill -9 $PID
fi


if [ "$1" == "build" ]; then
    echo "Compilando..."
    mvn clean install
fi


mvn exec:java -Dexec.mainClass="com.is1.proyecto.App"
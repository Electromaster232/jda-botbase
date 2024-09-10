#!/bin/sh

mvn install && mvn package
cd SnipeModule || exit
mvn package
cd ..
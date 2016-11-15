#!/bin/sh

set -e

ROOTDIR=/root/routing-engine/routing-engine-app-1.0.0-SNAPSHOT
# JAR="${ROOTDIR}/routing-engine-1.0.0-SNAPSHOT-jar-with-dependencies.jar"
LIBDIR="${ROOTDIR}/lib"
CONFIG="application.xml"

if [ -f "${ROOTDIR}/../conf/app.properties" ]; then
  cp "${ROOTDIR}/../conf/app.properties" "${ROOTDIR}/conf"
fi
java -cp "${LIBDIR}/*:${ROOTDIR}/conf" org.amlinv.routing.RoutingEngineApp "${CONFIG}"

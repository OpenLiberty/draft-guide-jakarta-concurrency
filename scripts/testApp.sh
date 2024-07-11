#!/bin/bash
set -euxo pipefail

cd inventory

mvn -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -q clean package liberty:create liberty:install-feature liberty:deploy

mvn -ntp liberty:start
sleep 5

curl -s http://localhost:9080/inventory/api/systems | grep "\\[\\]" || exit 1

mvn -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
     failsafe:integration-test

mvn -ntp failsafe:verify

mvn -ntp liberty:stop
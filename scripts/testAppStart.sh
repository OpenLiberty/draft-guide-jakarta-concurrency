#!/bin/bash
set -euxo pipefail

cd ../start

mvn -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -pl system -q clean package liberty:create liberty:install-feature liberty:deploy

mvn -ntp -pl system liberty:start

mvn -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -pl inventory -q clean package liberty:create liberty:install-feature liberty:deploy

mvn -ntp -pl inventory liberty:start

sleep 5

curl -s http://localhost:9081/api/inventory/systems | grep "\\[\\]" || exit 1

mvn -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -pl system failsafe:integration-test

mvn -ntp -pl system failsafe:verify

mvn -ntp -pl inventory liberty:stop
mvn -ntp -pl system liberty:stop
#!/bin/bash
while getopts t:d:j: flag; do
    case "${flag}" in
    t) DATE="${OPTARG}" ;;
    d) DRIVER="${OPTARG}" ;;
    j) JDK_LEVEL="${OPTARG}" ;;
    *) echo "Invalid option input" ;;
    esac
done

echo "Testing daily build image"

if [ "$JDK_LEVEL" == "11" ]; then
    echo "Test skipped because the guide does not support Java 11."
    exit 0
fi

if [ "$JDK_LEVEL" == "17" ]; then
    echo "Test skipped because the guide does not support Java 17."
    exit 0
fi

#sed -i "\#<artifactId>liberty-maven-plugin</artifactId>#a<configuration><install><runtimeUrl>https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/nightly/$DATE/$DRIVER</runtimeUrl></install></configuration>" pom.xml  ../start/pom.xml
sed -i "s;2025-10-23_0302/openliberty-all-25.0.0.12-cl251220251023-0302.zip;$DATE/$DRIVER;g" pom.xml ../start/pom.xml
cat pom.xml ../start/pom.xml

../scripts/testAppFinish.sh
../scripts/testAppStart.sh

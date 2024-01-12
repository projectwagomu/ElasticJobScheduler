#!/bin/bash

# The unique command is passed as an argument to this script
SPECIFIC_COMMAND="$@"

echo Nodes=$NODES
echo Hostfile=$NODE_FILE
echo SCHEDULER_IP=$SCHEDULER_IP
echo SCHEDULER_PORT=$SCHEDULER_PORT
echo SCRIPT_DIR=$SCRIPT_DIR
echo Launcher=$LAUNCHER
echo Workers=$WORKERS
THREADS=$((WORKERS * 2))
echo Threads=$THREADS
echo Malleable=$malleable
echo SPECIFIC_COMMAND=$SPECIFIC_COMMAND
echo GLBN=$GLBN

target="${SCRIPT_DIR}/../../../lifelineglb/target"

java -cp "${target}/*" \
  --add-modules java.se \
  --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.management/sun.management=ALL-UNNAMED \
  --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
  -Dapgas.verbose.launcher=true \
  -Dapgas.launcher=$LAUNCHER \
  -Dapgas.places=$NODES \
  -Dapgas.threads=$THREADS \
  -Dapgas.immediate.threads=$THREADS \
  -Dapgas.elastic=$malleable \
  -Dapgas.elastic.allatonce=false \
  -Dapgas.resilient=true \
  -Dapgas.backupcount=6 \
  -Dapgas.hostfile=$NODE_FILE \
  -Dapgas.consoleprinter=false \
  -Dglb.multiworker.workerperplace=$WORKERS \
  -Dglb.multiworker.n=$GLBN \
  -Dmalleable_scheduler_ip=$SCHEDULER_IP \
  -Dmalleable_scheduler_port=$SCHEDULER_PORT \
  $SPECIFIC_COMMAND

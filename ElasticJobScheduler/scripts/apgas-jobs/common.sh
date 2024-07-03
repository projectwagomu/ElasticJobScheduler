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
echo Elastic=$ELASTIC
echo SPECIFIC_COMMAND=$SPECIFIC_COMMAND
echo GLBN=$GLBN
echo JobName=$JOBNAME
echo Port=$PORT

export LOWLOAD=10
export HIGHLOAD=90
export SYNTH=evotree
export BRANCH=5000
export MODE=task


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
  -Dapgas.elastic=$ELASTIC \
  -Dapgas.elastic.allatonce=false \
  -Dapgas.lowload=$LOWLOAD \
  -Dapgas.highload=$HIGHLOAD \
  -Dapgas.evolving.mode=$MODE \
  -Dglb.synth=$SYNTH \
  -Dglb.synth.branch=$BRANCH \
  -Dapgas.resilient=true \
  -Dapgas.backupcount=6 \
  -Dapgas.hostfile=$NODE_FILE \
  -Dapgas.consoleprinter=false \
  -Dglb.multiworker.workerperplace=$WORKERS \
  -Dglb.multiworker.n=$GLBN \
  -Delastic_scheduler_ip=$SCHEDULER_IP \
  -Delastic_scheduler_port=$SCHEDULER_PORT \
  -Dhazelcast.name=$JOBNAME \
  -Dapgas.port=$PORT \
  $SPECIFIC_COMMAND

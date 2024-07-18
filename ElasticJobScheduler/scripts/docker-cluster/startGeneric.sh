#!/bin/bash
#
# Copyright (c) 2024 Wagomu project.
#
# This program and the accompanying materials are made available to you under
# the terms of the Eclipse Public License 2.0 which accompanies this distribution,
# and is available at https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#

CWD="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

mkdir -p ${CWD}/../../out

#Only needed for Slurm:
#timelimit=$TIMELIMIT
#partition=$PARTITION

numnodes=$NUMNODES
workspace="${CWD}/../.."
scheduler=$SCHEDULER
experiment=$(echo "$EXPERIMENT" | sed 's/\//\\\//g; s/&/\\\&/g; s/$/\\/')
expname=$EXPNAME
workers=$WORKERS
rand=$RAND

name="ElasticJobScheduler-nodes${numnodes}-${expname}"

thishost="n1"
tobeadded=""
for ((i=2; i<1+$numnodes; i++)); do
  tobeadded+="n$i "
done
minNodes="$((numnodes - 1))"

${workspace}/scripts/startScheduler.sh $scheduler ${rand}_${name} ${minNodes} job.launcher.SshLauncher apgas.launcher.SshLauncher ${workers} &

schedulerpid=$!

sleep 5

${workspace}/scripts/addNodes.sh $tobeadded

sleep 20

eval "$EXPERIMENT"

sleep 10

${workspace}/scripts/stopScheduler.sh

wait $schedulerpid

echo "startGeneric.sh in Docker-Cluster done"
#!/bin/bash
#
# Copyright (c) 2023 Wagomu project.
#
# This program and the accompanying materials are made available to you under
# the terms of the Eclipse Public License 2.0 which accompanies this distribution,
# and is available at https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#

CWD="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $CWD/../build/classes/

SCHEDULER="FCFS"
longuuid=$(cat /proc/sys/kernel/random/uuid)
SCHEDULERID=${longuuid:0:8}
MINNODES=0
LAUNCHER=job.launcher.SshLauncher
INSIDELAUNCHER=apgas.launcher.SshLauncher
WORKERS=4

if [ $# -lt 6 ]; then
  echo "Using default values as parameters. For manual configuration pass ALL parameters: <SCHEDULER> <ID> <MINNODES> <LAUNCHER> <INSIDE_LAUNCHER> <WORKERS>"
  echo "Available schedulers: Backfilling, EasyBackfilling, FCFS, MalleableStrategy"
  echo "Available launchers: job.launcher.SshLauncher, job.launcher.SrunLauncher"
  echo "Inside launcher is passed as parameter to the job"
  echo "Workers per process"
else
  SCHEDULER=$1
  SCHEDULERID=$2
  MINNODES=$3
  LAUNCHER=$4
  INSIDELAUNCHER=$5
  WORKERS=$6
fi

mkdir -p $CWD/../out

echo "Job Scheduling Algorithm: ${SCHEDULER}"
echo "Job Scheduling ID: ${SCHEDULERID}"
echo "Min Nodes: ${MINNODES}"
echo "Job Launcher: ${LAUNCHER}"
echo "Inside Job Launcher: ${INSIDELAUNCHER}"
echo "Workers: ${WORKERS}"

java -Dscheduler.strategies=${SCHEDULER} \
  -Dscheduler.id=${SCHEDULERID} \
  -Dscheduler.min.nodes=${MINNODES} \
  -Dscheduler.job.launcher=${LAUNCHER} \
  -Dscheduler.inside.job.launcher=${INSIDELAUNCHER} \
  -Dscheduler.job.workers=${WORKERS} \
  scheduler.Scheduler

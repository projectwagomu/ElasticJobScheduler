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

####### Mail Notify / Job Name / Comment #######
#SBATCH --mail-type=NONE
#SBATCH --job-name=JOBNAME

####### Constraints #######
#SBATCH --time=TIME
#SBATCH --partition=PARTITION
#SBATCH --exclusive
#SBATCH --mem=0
#SBATCH --mem-per-cpu=0

####### Node Info #######
#SBATCH --nodes=NUMNODES
#SBATCH --distribution=cyclic

####### Output #######
#SBATCH --output=WORKSPACE/out/FILENAME.out.%j
#SBATCH --error=WORKSPACE/out/FILENAME.err.%j

####### Script #######
thishost=$(hostname | cut -d. -f1)
allhosts=$(scontrol show hostnames)
tobeadded=$(echo "$allhosts" | tr ',' '\n' | grep -v "$thishost" | tr '\n' ',' | sed 's/,$//')
minNodes="$((NUMNODES - 1))"

#Job Launchers:
# - job.launcher.SrunLauncher
# - job.launcher.SshLauncher
# - job.launcher.MPILauncher
#APGAS Launchers:
# - apgas.launcher.SrunLauncher
# - apgas.launcher.SshLauncher
srun -N 1 -n 1 -w $thishost WORKSPACE/scripts/startScheduler.sh SCHEDULER ${SLURM_JOB_ID}_FILENAME ${minNodes} job.launcher.SrunLauncher apgas.launcher.SrunLauncher WORKERS &

schedulerpid=$!

sleep 5

WORKSPACE/scripts/addNodes.sh $tobeadded

sleep 20

EXPERIMENT

sleep 10

WORKSPACE/scripts/stopScheduler.sh

wait $schedulerpid

echo "slurm done"
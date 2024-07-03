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

CWD="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

mkdir -p ${CWD}/../../out

timelimit=$TIMELIMIT
partition=$PARTITION
numnodes=$NUMNODES
workspace="${CWD}/../.."
scheduler=$SCHEDULER
experiment=$(echo "$EXPERIMENT" | sed 's/\//\\\//g; s/&/\\\&/g; s/$/\\/')
expname=$EXPNAME
workers=$WORKERS

name="ElasticJobScheduler-nodes${numnodes}-${expname}"

FOO=$(mktemp)
sed "s|JOBNAME|${name}|g;
     s|TIME|${timelimit}|g;
     s|PARTITION|${partition}|g;
     s|NUMNODES|${numnodes}|g;
     s|FILENAME|${name}|g;
     s|WORKSPACE|${workspace}|g;
     s|SCHEDULER|${scheduler}|g;
     s|WORKERS|${workers}|g;
     s|EXPERIMENT|${experiment%?}|g;" <${CWD}/jobcommitGeneric.sh >${FOO}
sbatch ${FOO}
rm ${FOO}

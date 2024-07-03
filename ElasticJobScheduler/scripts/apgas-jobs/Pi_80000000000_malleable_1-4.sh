#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME Pi_80000000000_malleable_1-4
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 4

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.pi.StartPi -n 80000000000"

#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME Pi_600000000000_malleable_4-16
#REQUIRED_TIME 800000
#MIN_NODES 4
#MAX_NODES 16

common_dir="${SCRIPT_DIR}/"

malleable="malleable"
GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.pi.StartPi -n 600000000000"

#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME BC_17_malleable_1-4
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 4

common_dir="${SCRIPT_DIR}/"

GLBN=31
source ${common_dir}/common.sh "handist.glb.examples.bc.StartBC -n 17"

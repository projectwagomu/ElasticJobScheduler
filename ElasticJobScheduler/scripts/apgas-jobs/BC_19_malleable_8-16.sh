#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME BC_19_malleable_8-16
#REQUIRED_TIME 800000
#MIN_NODES 8
#MAX_NODES 16

common_dir="${SCRIPT_DIR}/"

malleable="malleable"
GLBN=31
source ${common_dir}/common.sh "handist.glb.examples.bc.StartBC -n 19"

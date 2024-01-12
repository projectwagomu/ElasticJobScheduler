#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME BC_18_malleable_2-16
#REQUIRED_TIME 800000
#MIN_NODES 2
#MAX_NODES 16

common_dir="${SCRIPT_DIR}/"

malleable="malleable"
GLBN=31
source ${common_dir}/common.sh "handist.glb.examples.bc.StartBC -n 18"

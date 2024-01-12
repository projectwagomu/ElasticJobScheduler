#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME BC_19_rigid_16
#REQUIRED_TIME 800000
#NODES 16

common_dir="${SCRIPT_DIR}/"

malleable=false
GLBN=31
source ${common_dir}/common.sh "handist.glb.examples.bc.StartBC -n 19"

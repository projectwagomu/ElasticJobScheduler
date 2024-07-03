#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME BC_18_rigid_4
#REQUIRED_TIME 800000
#NODES 4

common_dir="${SCRIPT_DIR}/"

GLBN=31
source ${common_dir}/common.sh "handist.glb.examples.bc.StartBC -n 18"

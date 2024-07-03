#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME Pi_80000000000_rigid_2
#REQUIRED_TIME 800000
#NODES 2

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.pi.StartPi -n 80000000000"

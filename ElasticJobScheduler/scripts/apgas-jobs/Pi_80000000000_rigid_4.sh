#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME Pi_80000000000_rigid_4
#REQUIRED_TIME 800000
#NODES 4

common_dir="${SCRIPT_DIR}/"

malleable=false
GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.pi.StartPi -n 80000000000"

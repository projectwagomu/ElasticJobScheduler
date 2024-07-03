#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME Pi_1200000000000_rigid_16
#REQUIRED_TIME 800000
#NODES 16

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.pi.StartPi -n 1200000000000"

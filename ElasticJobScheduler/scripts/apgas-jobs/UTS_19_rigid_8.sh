#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME UTS_19_rigid_4
#REQUIRED_TIME 800000
#NODES 8

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.uts.StartMultiworkerUTS -d 19"

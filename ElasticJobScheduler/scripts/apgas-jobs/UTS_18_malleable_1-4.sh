#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME UTS_18_malleable_1-4
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 4

common_dir="${SCRIPT_DIR}/"

malleable="malleable"
GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.uts.StartMultiworkerUTS -d 18"

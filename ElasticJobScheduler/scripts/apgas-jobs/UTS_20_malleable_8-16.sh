#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME UTS_20_malleable_8-16
#REQUIRED_TIME 800000
#MIN_NODES 8
#MAX_NODES 16

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.uts.StartMultiworkerUTS -d 20"+
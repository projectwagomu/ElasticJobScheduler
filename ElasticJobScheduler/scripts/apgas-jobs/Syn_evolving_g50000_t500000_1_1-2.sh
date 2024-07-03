#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS evolving
#JOB_NAME Syn_evolving_1_1-2
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 2

common_dir="${SCRIPT_DIR}/"

GLBN=1
source ${common_dir}/common.sh "handist.glb.examples.syntheticBenchmark.StartSynthetic -dynamic -b 0 -g 50000 -t 500000 -u 20 -s 1"

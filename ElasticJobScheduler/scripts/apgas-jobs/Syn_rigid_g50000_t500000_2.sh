#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME Syn_rigid_2
#REQUIRED_TIME 800000
#NODES 2

common_dir="${SCRIPT_DIR}/"

GLBN=1
source ${common_dir}/common.sh "handist.glb.examples.syntheticBenchmark.StartSynthetic -dynamic -b 0 -g 50000 -t 500000 -u 20 -s 2"

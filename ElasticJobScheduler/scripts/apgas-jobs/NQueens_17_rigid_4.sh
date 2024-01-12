#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME NQueens_17_rigid_4
#REQUIRED_TIME 800000
#NODES 4

common_dir="${SCRIPT_DIR}/"

malleable=false
GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.nqueens.StartNQueens -n 17 -t 12"

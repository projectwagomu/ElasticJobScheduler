#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME NQueens_15_rigid_4
#REQUIRED_TIME 800000
#NODES 4

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.nqueens.StartNQueens -n 15 -t 9"

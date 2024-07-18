#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME NQueens_15_malleable_1-6
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 6

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.nqueens.StartNQueens -n 15 -t 9"

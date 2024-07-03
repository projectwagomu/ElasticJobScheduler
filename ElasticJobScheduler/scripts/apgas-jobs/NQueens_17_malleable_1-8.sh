#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME NQueens_17_malleable_1-8
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 8

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.nqueens.StartNQueens -n 17 -t 12"

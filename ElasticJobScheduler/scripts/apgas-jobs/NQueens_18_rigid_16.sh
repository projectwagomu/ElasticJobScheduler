#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME NQueens_18_rigid_16
#REQUIRED_TIME 800000
#NODES 16

common_dir="${SCRIPT_DIR}/"

malleable=false
GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.nqueens.StartNQueens -n 18 -t 13"

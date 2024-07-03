#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS rigid
#JOB_NAME NQueens_18_rigid_8
#REQUIRED_TIME 800000
#NODES 8

common_dir="${SCRIPT_DIR}/"

GLBN=511
source ${common_dir}/common.sh "handist.glb.examples.nqueens.StartNQueens -n 18 -t 13"

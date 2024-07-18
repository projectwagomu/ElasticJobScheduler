#  Copyright (c) 2023 Wagomu project.
#
#  This program and the accompanying materials are made available to you under
#  the terms of the Eclipse Public License 2.0 which accompanies this distribution,
#  and is available at https://www.eclipse.org/legal/epl-v20.html
#
#  SPDX-License-Identifier: EPL-2.0

# scheduler_algo = ["FCFS", "Backfilling", "EasyBackfilling", "MalleableStrategy"]
scheduler_algo = ["EasyBackfilling", "MalleableStrategy"]
malleable_strategies = ["MalleableStrategy"]
num_jobs = 10
nodes = 7
cores_per_node = 1
timelimit = 60
submitrange = 3
jobs_without_delay = 5
partition = "NotUsed"
ex_name = "dockerConfig"
seeds = [42]
percentages_malleable = [0.5, 1]
written_sh_files = []


jobs_rigid = [
    {"file": "NQueens_15_rigid_4.sh", "weight": 1},
]

rigid_to_malleable_map = {
    "NQueens_15_rigid_4.sh": "NQueens_15_malleable_1-6.sh",
}

#  Copyright (c) 2023 Wagomu project.
#
#  This program and the accompanying materials are made available to you under
#  the terms of the Eclipse Public License 2.0 which accompanies this distribution,
#  and is available at https://www.eclipse.org/legal/epl-v20.html
#
#  SPDX-License-Identifier: EPL-2.0

# scheduler_algo = ["FCFS", "Backfilling", "EasyBackfilling", "MalleableStrategy"]
scheduler_algo = ["FCFS", "MalleableStrategy"]
malleable_strategies = ["MalleableStrategy"]
num_jobs = 10
nodes = 8
cores_per_node = 48
timelimit = 60
submitrange = 3
jobs_without_delay = 5
partition = "public2023"
ex_name = "small_config"
seeds = [42]
percentages_malleable = [0.5, 1]
written_sh_files = []


jobs_rigid = [
    {"file": "BC_17_rigid_2.sh", "weight": 1.5},
    {"file": "BC_17_rigid_4.sh", "weight": 1},
    {"file": "NQueens_17_rigid_2.sh", "weight": 1.5},
    {"file": "NQueens_17_rigid_4.sh", "weight": 1},
    {"file": "UTS_18_rigid_2.sh", "weight": 1.5},
    {"file": "UTS_18_rigid_4.sh", "weight": 0.5},
    {"file": "Pi_80000000000_rigid_2.sh", "weight": 1.5},
    {"file": "Pi_80000000000_rigid_4.sh", "weight": 1},
]

rigid_to_malleable_map = {
    "BC_17_rigid_2.sh": "BC_17_malleable_1-4.sh",
    "BC_17_rigid_4.sh": "BC_17_malleable_1-4.sh",
    "NQueens_17_rigid_2.sh": "NQueens_17_malleable_1-8.sh",
    "NQueens_17_rigid_4.sh": "NQueens_17_malleable_1-8.sh",
    "UTS_18_rigid_2.sh": "UTS_18_malleable_1-4.sh",
    "UTS_18_rigid_4.sh": "UTS_18_malleable_1-4.sh",
    "Pi_80000000000_rigid_2.sh": "Pi_80000000000_malleable_1-4.sh",
    "Pi_80000000000_rigid_4.sh": "Pi_80000000000_malleable_1-4.sh",
}

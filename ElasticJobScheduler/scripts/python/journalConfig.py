#  Copyright (c) 2023 Wagomu project.
#
#  This program and the accompanying materials are made available to you under
#  the terms of the Eclipse Public License 2.0 which accompanies this distribution,
#  and is available at https://www.eclipse.org/legal/epl-v20.html
#
#  SPDX-License-Identifier: EPL-2.0

scheduler_algo = ["FCFS", "Backfilling", "EasyBackfilling", "MalleableStrategy"]
num_jobs = 25
nodes = 32
cores_per_node = 48
timelimit = 60
submitrange = 3
jobs_without_delay = 5
partition = "public2023"
ex_name = "experimentsJournal"
seeds = [43, 44, 45, 46, 47, 48, 49, 50, 51, 52]
percentages_malleable = [0.2, 0.4, 0.6, 0.8, 1]
written_sh_files = []

jobs_rigid = [
    {"file": "BC_17_rigid_2.sh", "weight": 1.5},
    {"file": "BC_17_rigid_4.sh", "weight": 1},
    {"file": "BC_18_rigid_2.sh", "weight": 1.5},
    {"file": "BC_18_rigid_4.sh", "weight": 1},
    {"file": "BC_18_rigid_8.sh", "weight": 0.5},
    {"file": "BC_19_rigid_8.sh", "weight": 1},
    {"file": "BC_19_rigid_16.sh", "weight": 0.5},
    {"file": "NQueens_17_rigid_2.sh", "weight": 1.5},
    {"file": "NQueens_17_rigid_4.sh", "weight": 1},
    {"file": "NQueens_18_rigid_8.sh", "weight": 1},
    {"file": "NQueens_18_rigid_16.sh", "weight": 0.5},
    {"file": "UTS_18_rigid_2.sh", "weight": 1.5},
    {"file": "UTS_18_rigid_4.sh", "weight": 0.5},
    {"file": "UTS_19_rigid_2.sh", "weight": 1.5},
    {"file": "UTS_19_rigid_4.sh", "weight": 1},
    {"file": "UTS_19_rigid_8.sh", "weight": 1},
    {"file": "UTS_20_rigid_8.sh", "weight": 1},
    {"file": "UTS_20_rigid_16.sh", "weight": 0.5},
    {"file": "Pi_80000000000_rigid_2.sh", "weight": 1.5},
    {"file": "Pi_80000000000_rigid_4.sh", "weight": 1},
    {"file": "Pi_600000000000_rigid_4.sh", "weight": 1.5},
    {"file": "Pi_600000000000_rigid_8.sh", "weight": 1},
    {"file": "Pi_600000000000_rigid_16.sh", "weight": 0.5},
    {"file": "Pi_1200000000000_rigid_8.sh", "weight": 1},
    {"file": "Pi_1200000000000_rigid_16.sh", "weight": 0.5},
]

rigid_to_malleable_map = {
    "BC_17_rigid_2.sh": "BC_17_malleable_1-4.sh",
    "BC_17_rigid_4.sh": "BC_17_malleable_1-4.sh",
    "BC_18_rigid_2.sh": "BC_18_malleable_2-16.sh",
    "BC_18_rigid_4.sh": "BC_18_malleable_2-16.sh",
    "BC_18_rigid_8.sh": "BC_18_malleable_2-16.sh",
    "BC_19_rigid_8.sh": "BC_19_malleable_8-16.sh",
    "BC_19_rigid_16.sh": "BC_19_malleable_8-16.sh",
    "NQueens_17_rigid_2.sh": "NQueens_17_malleable_1-8.sh",
    "NQueens_17_rigid_4.sh": "NQueens_17_malleable_1-8.sh",
    "NQueens_18_rigid_8.sh": "NQueens_18_malleable_8-16.sh",
    "NQueens_18_rigid_16.sh": "NQueens_18_malleable_8-16.sh",
    "UTS_18_rigid_2.sh": "UTS_18_malleable_1-4.sh",
    "UTS_18_rigid_4.sh": "UTS_18_malleable_1-4.sh",
    "UTS_19_rigid_2.sh": "UTS_19_malleable_2-16.sh",
    "UTS_19_rigid_4.sh": "UTS_19_malleable_2-16.sh",
    "UTS_19_rigid_8.sh": "UTS_19_malleable_2-16.sh",
    "UTS_20_rigid_8.sh": "UTS_20_malleable_8-16.sh",
    "UTS_20_rigid_16.sh": "UTS_20_malleable_8-16.sh",
    "Pi_80000000000_rigid_2.sh": "Pi_80000000000_malleable_1-4.sh",
    "Pi_80000000000_rigid_4.sh": "Pi_80000000000_malleable_1-4.sh",
    "Pi_600000000000_rigid_4.sh": "Pi_600000000000_malleable_4-16.sh",
    "Pi_600000000000_rigid_8.sh": "Pi_600000000000_malleable_4-16.sh",
    "Pi_600000000000_rigid_16.sh": "Pi_600000000000_malleable_4-16.sh",
    "Pi_1200000000000_rigid_8.sh": "Pi_1200000000000_malleable_8-16.sh",
    "Pi_1200000000000_rigid_16.sh": "Pi_1200000000000_malleable_8-16.sh",
}
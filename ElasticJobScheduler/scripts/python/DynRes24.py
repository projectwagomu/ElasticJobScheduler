#  Copyright (c) 2023 Wagomu project.
#
#  This program and the accompanying materials are made available to you under
#  the terms of the Eclipse Public License 2.0 which accompanies this distribution,
#  and is available at https://www.eclipse.org/legal/epl-v20.html
#
#  SPDX-License-Identifier: EPL-2.0

# scheduler_algo = ["FCFS", "Backfilling", "EasyBackfilling", "MalleableStrategy"]
scheduler_algo = ["EasyBackfilling", "EvolvingEasyBackfilling"]
malleable_strategies = ["EvolvingEasyBackfilling"]
num_jobs = 20
nodes = 10
cores_per_node = 12
timelimit = 45
submitrange = 10
jobs_without_delay = 5
partition = "FB16"
ex_name = "DynRes24"
seeds = [42, 43, 44, 45, 46]
percentages_malleable = [0.2, 0.4, 0.6, 0.8, 1]
written_sh_files = []

jobs_rigid = [
    {"file": "Syn_rigid_g50000_t500000_1.sh", "weight": 1},
    {"file": "Syn_rigid_g50000_t500000_2.sh", "weight": 1},
    {"file": "Syn_rigid_g50000_t500000_4.sh", "weight": 1},
    {"file": "Syn_rigid_g50000_t500000_8.sh", "weight": 1},
]

rigid_to_malleable_map = {
    "Syn_rigid_g50000_t500000_1.sh" : "Syn_evolving_g50000_t500000_1_1-2.sh",
    "Syn_rigid_g50000_t500000_2.sh" : "Syn_evolving_g50000_t500000_2_1-4.sh",
    "Syn_rigid_g50000_t500000_4.sh" : "Syn_evolving_g50000_t500000_4_2-8.sh",
    "Syn_rigid_g50000_t500000_8.sh" : "Syn_evolving_g50000_t500000_8_4-16.sh",
}
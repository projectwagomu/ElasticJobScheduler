#  Copyright (c) 2023 Wagomu project.
#
#  This program and the accompanying materials are made available to you under
#  the terms of the Eclipse Public License 2.0 which accompanies this distribution,
#  and is available at https://www.eclipse.org/legal/epl-v20.html
#
#  SPDX-License-Identifier: EPL-2.0

import os
import random
import stat
import string
import sys
import importlib

script_dir = os.path.dirname(os.path.abspath(__file__))
if script_dir not in sys.path:
    sys.path.insert(0, script_dir)

written_sh_files = []

# Explicitly declare variables filled with dummy values
scheduler_algo = [""]
num_jobs = 0
nodes = 0
cores_per_node = 0
timelimit = 0
submitrange = 0
jobs_without_delay = 0
partition = ""
ex_name = ""
seeds = [0]
percentages_malleable = [0]
jobs_rigid = [ {"x": "y", "z": 0} ]
rigid_to_malleable_map = { "x": "y" }

# List of expected configuration variables
config_variables = ['scheduler_algo',
                    'num_jobs',
                    'nodes',
                    'cores_per_node',
                    'timelimit',
                    'submitrange',
                    'jobs_without_delay',
                    'partition',
                    'ex_name',
                    'seeds',
                    'percentages_malleable',
                    'jobs_rigid',
                    'rigid_to_malleable_map'
                    ]

def generate_random_string(length=6):
    letters = string.ascii_letters + string.digits
    return ''.join(random.choice(letters) for i in range(length))

# Generate a 100% rigid job set
def generate_rigid_job_set(num_jobs):
    job_set = random.choices(
        [job["file"] for job in jobs_rigid],
        weights=[job["weight"] for job in jobs_rigid],
        k=num_jobs
    )
    timestamps = [random.randint(0, submitrange * 60 - 1) for _ in job_set]
    job_timestamp_pairs = list(zip(job_set, timestamps))
    sorted_jobs = sorted(job_timestamp_pairs, key=lambda x: x[1])
    return sorted_jobs


# Convert a rigid job to its malleable counterpart
def convert_to_malleable(rigid_job):
    return rigid_to_malleable_map.get(rigid_job, rigid_job)


# Generate job sets with increasing percentages of malleable jobs
def generate_mixed_job_set(base_set, percentage_malleable):
    num_to_convert = int(len(base_set) * percentage_malleable)

    # Randomly select indices to be converted to malleable
    malleable_indices = random.sample(range(len(base_set)), num_to_convert)

    converted_jobs = []
    for index, (job, timestamp) in enumerate(base_set):
        if index in malleable_indices:
            converted_jobs.append((convert_to_malleable(job), timestamp))
        else:
            converted_jobs.append((job, timestamp))
    return converted_jobs


def write_to_sh_file(sorted_jobs, path, scheduler, percentage, seed, random_string):
    filename = path + ex_name + "/" + ex_name + "_" + scheduler + "_" + percentage + "_" + str(seed) + ".sh"
    print(filename)
    written_sh_files.append(filename)

    with open(filename, 'w') as file:

        file.write("#!/bin/bash" + '\n')
        file.write('CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"' + '\n')
        file.write('cd ${CWD}' + '\n')
        file.write('apgasjobs="${CWD}/../../apgas-jobs/"' + '\n')
        file.write('jobsubmit="${CWD}/../../submitJob.sh"' + '\n')

        file.write('\n' + '#Params' + '\n')
        file.write('export TIMELIMIT=' + str(timelimit) + '\n')
        file.write('export PARTITION=' + partition + '\n')
        file.write('export NUMNODES=' + str(nodes + 1) + '\n')
        file.write('export WORKERS=' + str(cores_per_node) + '\n')
        file.write('export SCHEDULER=' + scheduler + '\n')
        file.write('export EXPNAME=' + ex_name + '_' + scheduler + '_' + percentage + '_' + str(seed) + '\n')
        file.write('export RAND=' + random_string + '\n')
        file.write('export EXPERIMENT="' + '\n')
        for index, (job, timestamp) in enumerate(sorted_jobs):
            if index < jobs_without_delay:
                file.write('${jobsubmit} ${apgasjobs}' + job + '\n')
            else:
                # For the rest, calculate sleep time based on the difference between current and previous timestamp
                sleep_time = timestamp - sorted_jobs[index - 1][1]
                file.write(f"sleep {sleep_time}\n")
                file.write('${jobsubmit} ${apgasjobs}' + job + '\n')

        file.write('"' + '\n')
        file.write('./../startGeneric.sh' + '\n')

    st = os.stat(filename)
    os.chmod(filename, st.st_mode | stat.S_IEXEC)


def writer_start_experiment_file(start_experiment_path, sh_list):
    with open(start_experiment_path, 'w') as file:
        file.write("#!/bin/bash\n")
        file.write('CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"\n')
        file.write('cd ${CWD}\n\n')
        for sh_file in sh_list:
            file.write(f"./{sh_file}\n")
    st = os.stat(start_experiment_path)
    os.chmod(start_experiment_path, st.st_mode | stat.S_IEXEC)

def load_config(module_name):
    try:
        config_module = importlib.import_module(module_name)
        return {var: getattr(config_module, var) for var in config_variables}
    except ImportError:
        print(f"Configuration module '{module_name}' not found.")
        sys.exit(1)

if __name__ == '__main__':
    config_module_name = "smallConfig"
    if len(sys.argv) > 1:
        config_module_name = sys.argv[1]
    else:
        print("Usage: python script.py <config_module> [slurm|docker-cluster]")
        sys.exit(1)

    config = load_config(config_module_name)
    print("Config: ", config_module_name)

    if len(sys.argv) > 2:
        environment = sys.argv[2]
        if environment not in ["slurm", "docker-cluster"]:
            print("Second argument must be 'slurm' or 'docker-cluster'.")
            sys.exit(1)
    else:
        print("Second argument could be 'slurm' or 'docker-cluster'.")
        environment = "slurm"

    for var in config_variables:
        globals()[var] = config[var]

    args = sys.argv
    path = args[0]
    path = path.replace("generateExperiments.py", "")
    path = path + f"../{environment}/"
    print("Path: ", path)
    print("Experiment: ", path + ex_name)

    if not os.path.exists(path + ex_name):
        os.makedirs(path + ex_name)

    random_string = generate_random_string()

    for s in seeds:
        for algo in scheduler_algo:
            random.seed(s)

            rigid_job_set = generate_rigid_job_set(num_jobs)

            write_to_sh_file(rigid_job_set, path, algo, "0", s, random_string)

            for percentage in percentages_malleable:
                random.seed(s)
                job_set = generate_mixed_job_set(rigid_job_set, percentage)
                write_to_sh_file(job_set, path, algo, f"{int(percentage * 100)}", s, random_string)

    # Write start_experiment files
    pathWithName = os.path.join(path, ex_name)
    all_sh_files = sorted([f for f in os.listdir(pathWithName) if f.endswith('.sh') and not f.startswith("start_")])
    written_sh_files = [os.path.basename(f) for f in written_sh_files]
    writer_start_experiment_file(os.path.join(pathWithName, f"start_{ex_name}_all.sh"), all_sh_files)
    writer_start_experiment_file(os.path.join(pathWithName, f"start_{ex_name}.sh"), written_sh_files)

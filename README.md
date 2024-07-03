# Elastic Job Scheduler

This project introduces an **Elastic Job Scheduler** that focuses on scheduling HPC jobs in an elastic way. This scheduler is expertly designed to dynamically adjust the size of running jobs, allowing them to expand or contract in terms of the number of nodes used. Currently, only APGAS jobs are supported, but the architecture is versatile allowing for the seamless integration of other job types. In addition, four job scheduling algorithms are provided, but the design allows for the easy addition of new algorithms.

This scheduler is an advanced iteration of the *FIFO Malleable Job Scheduler*: https://github.com/projectwagomu/FIFO-Malleable-Job-Scheduler

## Features
 - Extensible Architecture: Easily adaptable to accommodate different types of both job types and job scheduling algorithms.
 - Slurm Cluster Integration: Ready for running in Slurm clusters.
 - Comprehensive Experimentation and Analysis Tools: Includes scripts for generating job sets and analyzing performance regarding multiple metrics.

### Requirements
- Java
- Maven
- Ant
- Python 3.9
   - `pip3 install -r ElasticJobScheduler/scripts/python/requirements.txt`

## Build Instructions

To build this whole project (including clone and compile APGAS and GLB as necessary components), run:

```shell
./ElasticJobScheduler/scripts/cloneAndCompileAll.sh
```

## Usage

The following four scripts are used to interact with the job scheduler.

```shell
./ElasticJobScheduler/scripts/startScheduler.sh
./ElasticJobScheduler/scripts/addNode.sh nodename
./ElasticJobScheduler/scripts/submitJob.sh /path/to/job.sh
./ElasticJobScheduler/scripts/stopScheduler.sh
```

`startScheduler.sh` should be launched in the background on the host in charge of running the scheduler. After launching this process in the background, the hosts on which computation can be performed should be added using the `addNode.sh` script.

To submit a job, script `submitJob.sh` should be used with the path to the job script given as argument.

`stopScheduler.sh` can be used after all the jobs have completed their execution to cleanly shut down the scheduler.

## Launching a batch on a Slurm Cluster

First, generate job sets with custom parameters:

```shell
python3 ElasticJobScheduler/scripts/python/generateExperiments.py
```

By default, [generateExperiments.py](ElasticJobScheduler%2Fscripts%2Fpython%2FgenerateExperiments.py) loads the configuration file [smallConfig.py](ElasticJobScheduler%2Fscripts%2Fpython%2FsmallConfig.py) that includes the following:

```
scheduler_algo = ["FCFS", "MalleableStrategy"]
num_jobs = 10
nodes = 8
cores_per_node = 48
timelimit = 60
submitrange = 3
jobs_without_delay = 5
partition = "pub23"
ex_name = "experiments"
seeds = [42]
```

Using these parameters, seed 42 orchestrates the creation of diverse job sets for two scheduling strategies: FCFS and MalleableStrategy. Specifically, it generates three types of job sets:

 - 100% Rigid Job Set: All jobs are rigid, without flexibility in resource allocation.
 - Mixed Job Set: A balanced mix of 50% malleable and 50% rigid jobs.
 - 100% Malleable Job Set: Every job in this set is malleable, offering full flexibility in resource usage.

Each job set comprises 10 individual jobs, and these are designed to be executed across 9 compute nodes. To facilitate this, a total of 17 nodes are requisitioned, with one dedicated node exclusively running the Elastic Job Scheduler. The job submission is strategically timed: five jobs are initiated at timestep 0, with the remainder being dispatched within the first three minutes.

This process is carried out on a Slurm cluster, specifically using the `public2023` partition. Each node in this partition is equipped with 48 cores.

To use your own configuration file `myConfig.py`:

```shell
python3 ElasticJobScheduler/scripts/python/generateExperiments.py myConfig
```

After experiment generation, you can submit each individual experiment as a Slurm job using the `start_experiments.sh` script prepared in the relevant directory:

```shell
./ElasticJobScheduler/scripts/slurm/experiments/start_experiments.sh
```

This way, both scripts [jobcommitGeneric.sh](ElasticJobScheduler%2Fscripts%2Fslurm%2FjobcommitGeneric.sh) and [startGeneric.sh](ElasticJobScheduler%2Fscripts%2Fslurm%2FstartGeneric.sh) are used. A key aspect of this setup is defining the routine for initiating jobs. Currently, there are three options available: `ssh`, `srun`, and `mpirun`. In our configuration, we utilize `srun` due to its integration and optimization with Slurm environments.

While the script `start_experiments.sh` contains always only the newest generated experiments, the script `start_experiments_all.sh` contains all experiments located in the folder. This can be usefull if changes are made to the configuration file to start more experiments.

Results are stored in the [out](ElasticJobScheduler%2Fout) directory.

After the completion of Slurm jobs, analyze the outputs:

```shell
python3 ./ElasticJobScheduler/scripts/python/plotExperiments.py ElasticJobScheduler/out
```

Again, the configuration [smallConfig.py](ElasticJobScheduler%2Fscripts%2Fpython%2FsmallConfig.py) is used by default, but you can specify your own configuration:

```shell
python3 ./ElasticJobScheduler/scripts/python/plotExperiments.py ElasticJobScheduler/out myConfig
```

Analysis results (CSV and PDF formats) are stored in the [_analyses](ElasticJobScheduler%2Fout%2F_analyses) directory.


## License

This software is released under the terms of the [Eclipse Public License v2.0](LICENSE.txt), though it also uses third-party packages with their own licensing terms.

## Publications

- The Impact of Evolving APGAS Programs on HPC Clusters (to appear)
- On the Performance of Malleable APGAS Programs and Batch Job Schedulers (https://doi.org/10.1007/s42979-024-02641-7)
  - Artefact: https://github.com/ProjectWagomu/ArtefactSNCS24

## Contributors

In alphabetical order:

- Janek Bürger
- Patrick Finnerty
- Jonas Posner



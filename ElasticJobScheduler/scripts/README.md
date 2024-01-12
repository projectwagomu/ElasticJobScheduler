# Scripts

This directory contains a number of scripts that accompany the elastic job scheduler.
Together, these provide a framework to create and submit a batch of rigid/elastic jobs encapsulated in a single job.

## Compilation helper scripts

- **cloneAndCompileAll.sh** downloads the APGAS for Java library and the lifeline-based GLB to the root directory of this repository. Compilation of these projects is then performed using Maven, followed by the compilation of the job scheduler contained in this repository. This script should be used to make sure the programs used in the batch jobs provided as example in the [scripts/apgas-jobs](scripts/apgas-jobs) directory can be used.
- **compile.sh** compiles the job scheduler contained in this repository.


## Job Scheduler control scripts

The following four scripts are used to interact with the job scheduler.

- `./ElasticJobScheduler/scripts/startScheduler.sh`
- `./ElasticJobScheduler/scripts/addNode.sh nodename`
- `./ElasticJobScheduler/scripts/submitJob.sh /path/to/job.sh`
- `./ElasticJobScheduler/scripts/stopScheduler.sh`

`startScheduler.sh` should be launched in the background on the host in charge of running the scheduler. After launching this processin the brackground, the hosts on which computation can be performed should be added using the `addNode.sh` script.

To submit a job, script `submitJob.sh` should be used with the path to the job script given as argument.

`stopScheduler.sh` can be used after all the jobs have completed their execution to cleanly shut down the scheduler.

## Example APGAS job scripts

These scripts can be found in directory [`apgas-jobs`](./apgas-jobs). These are all executions of the lifeline-based GLB. As such every _shell_ script in this directory defines the specific arguments and environment variables necessary for this particular job before calling the `common.sh` script in which all the common settings are defined.

Scripts for four benchmarks are provided, with various parameters to adjust their sizes:
- **BC** (Betweeness Centrality)
- **N-Queens**
- **Pi**
- **UTS** (Unbalanced Tree Search)

The implementation of these benchmarks can be found in the [lifelineGLB](https://github.com/projectwagomu/lifelineglb/tree/master/src/main/java/handist/glb/examples  repository (cloned and compiled through the use of the `cloneAndCompileAll.sh` script mentioned above).

There are multiple scripts, some rigid, and some malleable. All should define a number of settings at the top of the script. For instance, a malleable jobs script should include the following information:

```shell
#!/bin/bash
#JOB_TYPE apgas
#JOB_CLASS malleable
#JOB_NAME BC_17_malleable_1-4
#REQUIRED_TIME 800000
#MIN_NODES 1
#MAX_NODES 4

...
```

These settings define:
- `JOB_TYPE` how the scheduler should interact with the job (in this case through the apgas defined API)
- `JOB_CLASS` if the job is `rigid` or `malleable`
- `JOB_NAME` to give a name to the job
- `REQUIRED_TIME` time needed to complete the job, in seconds
- `NODES` number of nodes this job requries (for rigid jobs)
- `MIN_NODES` minimum number of nodes this job can run with (for malleable jobs only)
- `MAX_NODES` maximum number of nodes this job can run with (for malleable jobs only)



## Malleable APGAS Batch generation, execution and analysis

These scripts can be found in the [`python`](./python) directory.

### Generation

These scripts can be found in directory `python`.
Script `generateExperiment.py` can be used to generate multiple jobs based on multiple seeds.
The configuration to use can be passed as argument to this script, if no argument is provided, `smallConfig.py` by default.

**Default generation**

```shell
$ python3 generateExperiments.py
Config:  smallConfig
Path:  ../slurm/
Experiment:  ../slurm/small_config
../slurm/small_config/small_config_FCFS_0_42.sh
../slurm/small_config/small_config_FCFS_50_42.sh
../slurm/small_config/small_config_FCFS_100_42.sh
../slurm/small_config/small_config_MalleableStrategy_0_42.sh
../slurm/small_config/small_config_MalleableStrategy_50_42.sh
../slurm/small_config/small_config_MalleableStrategy_100_42.sh
```

**Specifying a configuration**

```shell
$ python3 generateExperiments.py journalConfig
Config:  journalConfig
Path:  ../slurm/
Experiment:  ../slurm/experimentsJournal
../slurm/experimentsJournal/experimentsJournal_FCFS_0_43.sh
../slurm/experimentsJournal/experimentsJournal_FCFS_20_43.sh
[...]
../slurm/experimentsJournal/experimentsJournal_MalleableStrategy_60_52.sh
../slurm/experimentsJournal/experimentsJournal_MalleableStrategy_80_52.sh
../slurm/experimentsJournal/experimentsJournal_MalleableStrategy_100_52.sh
```

These configuration files allow you to specify which scheduling algorithms should be used, the job scripts to use, as well as the seeds to use to generate the random batch. 
The [`journalConfig.py`](python/journalConfig) file contains the configuration used for our article **[On the Performance of Malleable APGAS Programs and Batch Job Schedulers](https://doi.org/)**.

To run a batch on your own Slurm cluster, you may have to adjust some parameters inside the configuration files, such as the "partition," "nodes," and the "cores_per_node" settings to reflect your particular environment.

### Execution

So submit every batch as a Slurm job, use the `start_<experiment name>_all.sh` script generated along the various configurations in the `script/slurm/<experiment name> ` directory.
This script will in turn rely on the `jobcommitGeneric.sh` and `startGeneric.sh` scripts found in the [`slurm`](directory).

### Analysis

Similar to the batch generation, the analyses can be run after completion of every batch using the following command:

```shell
$ python3 plotExperiments
```


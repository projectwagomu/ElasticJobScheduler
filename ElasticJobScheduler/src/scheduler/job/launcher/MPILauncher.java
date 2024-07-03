/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.job.launcher;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

public class MPILauncher implements Launcher {
  @Override
  public ProcessBuilder launch(Job job, List<Node> nodes, File file) {
    final String elastic;
    if (Configuration.CONFIG_SCHEDULER_STRATEGIES.get().toLowerCase().contains("evolving")
        && job.getJobName().toLowerCase().contains("evolving")) {
      elastic = "evolving";
    } else if (Configuration.CONFIG_SCHEDULER_STRATEGIES.get().toLowerCase().contains("malleable")
        && job.getJobName().toLowerCase().contains("malleable")) {
      elastic = "malleable";
    } else {
      elastic = "rigid";
    }

    String[] execString = {
      "mpirun",
      "--bind-to",
      "none",
      "-x",
      "NODES=" + nodes.size(),
      "-x",
      "NODE_FILE=" + file.getAbsolutePath(),
      "-x",
      "SCHEDULER_IP=" + Resources.SCHEDULER_IP,
      "-x",
      "SCHEDULER_PORT=" + Constants.SCHEDULER_PORT,
      "-x",
      "SCRIPT_DIR=" + job.scriptDir,
      "-x",
      "LAUNCHER=" + Configuration.CONFIG_SCHEDULER_INSIDE_JOB_LAUNCHER.get(),
      "-x",
      "WORKERS=" + Configuration.CONFIG_SCHEDULER_JOB_WORKERS.get(),
      "ELASTIC=" + elastic,
      "JOBNAME=" + job.getId(),
      "PORT=" + (5701 + job.getId()),
      "-host",
      nodes.get(0).getId(),
      job.scriptPath.toString(),
    };

    Configuration.logger.log(
        "JobID:" + job.getId() + " :: create execString >> " + Arrays.toString(execString));

    return new ProcessBuilder(execString);
  }
}

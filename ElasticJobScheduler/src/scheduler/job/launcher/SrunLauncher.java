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

public class SrunLauncher implements Launcher {
  @Override
  public ProcessBuilder launch(Job job, List<Node> nodes, File file) {
    String[] execString = {
      "srun",
      "-N 1",
      "-n 1",
      "--export=ALL,NODES="
          + nodes.size()
          + ",NODE_FILE="
          + file.getAbsolutePath()
          + ",SCHEDULER_IP="
          + Resources.SCHEDULER_IP
          + ",SCHEDULER_PORT="
          + Constants.SCHEDULER_PORT
          + ",SCRIPT_DIR="
          + job.scriptDir
          + ",LAUNCHER="
          + Configuration.CONFIG_SCHEDULER_INSIDE_JOB_LAUNCHER.get()
          + ",WORKERS="
          + Configuration.CONFIG_SCHEDULER_JOB_WORKERS.get(),
      "-w",
      nodes.get(0).getId(),
      job.scriptPath.toString(),
    };

    Configuration.logger.log(
        "JobID:" + job.getId() + " :: create execString >> " + Arrays.toString(execString));

    return new ProcessBuilder(execString);
  }
}

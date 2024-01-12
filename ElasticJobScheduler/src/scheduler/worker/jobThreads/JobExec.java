/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.worker.jobThreads;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

/** Runs a job on another node via JobLauncher */
public class JobExec extends Thread {

  public final LinkedBlockingQueue<JobInfoSheet> jobInfoSheets = new LinkedBlockingQueue<>();
  private boolean isActive = true;

  @Override
  public synchronized void run() {
    Configuration.logger.log("Start");
    while (this.isActive) {
      try {
        if (this.jobInfoSheets.isEmpty()) {
          wait(Constants.jobObserverSleepInMillis);
        }

        while (!this.jobInfoSheets.isEmpty()) {
          JobInfoSheet jobInfoSheets = this.jobInfoSheets.take();
          Job job = jobInfoSheets.job;
          List<Node> nodes = jobInfoSheets.nodes;
          if (jobInfoSheets.execCommand.equals(Constants.JOB_EXEC)) {
            job.exec(nodes);

            Resources.mutex.acquire();
            Resources.runningJobs.add(job);
            Resources.mutex.release();

            for (Node node : nodes) {
              node.setJob(job);
              Resources.workingNodes.add(node);
            }
          }
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    Configuration.logger.log("End");
  }

  public synchronized void deactivate() {
    this.isActive = false;
    notify();
  }

  public boolean add(Job job, List<Node> nodes, String execCommand) {
    return this.jobInfoSheets.add(new JobInfoSheet(job, nodes, execCommand));
  }

  private class JobInfoSheet {
    public final Job job;
    public final List<Node> nodes;
    public final String execCommand;

    JobInfoSheet(Job job, List<Node> nodes, String execCommand) {
      this.job = job;
      this.nodes = nodes;
      this.execCommand = execCommand;
    }
  }
}

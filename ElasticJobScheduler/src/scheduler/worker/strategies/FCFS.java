/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.worker.strategies;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

public class FCFS extends SchedulingStrategy {

  final Queue<Job> jobs = new ArrayDeque<>();

  @Override
  public void run() {
    Configuration.logger.log(
        "All Nodes: "
            + Resources.allNodes.size()
            + "; Free Nodes: "
            + Resources.openNodes.size()
            + "; Working Nodes: "
            + Resources.workingNodes.size()
            + "; Running Jobs: "
            + Resources.runningJobs.size()
            + "; Waiting Jobs: "
            + this.jobs.size()
            + "; Waiting Jobs (Receiver): "
            + Resources.openNewJobs.size());

    this.checkForNewJobs();

    if (jobs.isEmpty()) return;

    while (!this.jobs.isEmpty()) {
      Job job = jobs.peek();
      if (job.getMinNode() <= Resources.openNodes.size()) {
        try {
          List<Node> nodes = new LinkedList<>();
          while (nodes.size() < job.getMaxNode() && !Resources.openNodes.isEmpty()) {
            Node node = Resources.openNodes.take();
            nodes.add(node);
          }

          Configuration.jobExec.add(job, nodes, Constants.JOB_EXEC);

          jobs.poll();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

      } else {
        break;
      }
    }
  }

  @Override
  public void checkForNewJobs() {
    LinkedBlockingQueue<Job> openNewJobs = Resources.openNewJobs;
    Job newJob;
    while (!openNewJobs.isEmpty()) {
      try {
        newJob = openNewJobs.take();
        if (newJob.parse()) {
          this.jobs.add(newJob);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

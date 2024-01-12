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

import java.util.LinkedList;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

/** Checks if a job is still active */
public class JobObserver extends Thread {

  private boolean isActive = true;

  @Override
  public void run() {
    Configuration.logger.log("Start");
    while (this.isActive) {
      try {
        sleep(Constants.jobObserverSleepInMillis);

        Resources.mutex.acquire();
        LinkedList<Job> copyRunningJobs = new LinkedList<>(Resources.runningJobs);
        Resources.mutex.release();

        for (Job job : copyRunningJobs) {
          if (!job.isActive() && !job.isJobOverdue()) {

            Resources.mutex.acquire();
            Resources.runningJobs.remove(job);
            Resources.mutex.release();
            Configuration.logger.log("JobID:" + job.getId() + " :: is done");

            // Cleaning always all nodes/jobs/processes
            job.cleanNodes();

            for (Node node : job.getNodes()) {
              Configuration.logger.log(
                  "JobID:" + job.getId() + " :: is done >> free Node: " + node.getId());
              node.setJob(null);
              Resources.workingNodes.remove(node);
              Resources.openNodes.put(node);
            }
            Configuration.worker.wakeUpThread();
          }
          if (job.isJobOverdue()) {
            Configuration.logger.log("JobID:" + job.getId() + " :: is Overdue");
            Resources.mutex.acquire();
            Resources.runningJobs.remove(job);
            Resources.mutex.release();
            Configuration.terminator.add(job);
          }
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    Configuration.logger.log("End");
  }

  public void deactivate() {
    this.isActive = false;
  }
}

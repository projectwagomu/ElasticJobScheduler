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

import java.util.concurrent.LinkedBlockingQueue;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

public class JobTerminator extends Thread {
  public final LinkedBlockingQueue<Job> jobs = new LinkedBlockingQueue<>();
  private boolean isActive = true;

  public synchronized void run() {
    Configuration.logger.log(
        "Terminator Start: \"You just can’t go around killing running processes\"");
    while (this.isActive) {
      try {
        if (this.jobs.isEmpty()) {
          wait(Constants.jobObserverSleepInMillis);
        } else {
          Job job = this.jobs.take();
          Configuration.logger.log("JobID:" + job.getId() + " :: start kill Process");
          Configuration.logger.log("Terminator: \"Hasta la vista, baby.\"");
          job.cleanNodes();
          Configuration.logger.log("JobID:" + job.getId() + " :: is done");
          for (Node node : job.getNodes()) {
            Configuration.logger.log(
                "JobID:" + job.getId() + " :: is done >> free Node: " + node.getId());
            node.setJob(null);
            Resources.workingNodes.remove(node);
            Resources.openNodes.put(node);
          }
          Configuration.worker.wakeUpThread();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    Configuration.logger.log("Terminator End: \"I´ll be back!\"");
  }

  public synchronized void deactivate() {
    this.isActive = false;
    notify();
  }

  public boolean add(Job job) {
    try {
      this.jobs.put(job);
    } catch (InterruptedException e) {
      Configuration.logger.log(e.toString());
      return false;
    }
    return true;
  }
}

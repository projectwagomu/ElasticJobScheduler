/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.worker;

import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.worker.jobThreads.JobExec;
import scheduler.worker.jobThreads.JobObserver;
import scheduler.worker.jobThreads.JobTerminator;

/**
 * Executes strategy after a certain time or when a job comes from the receiver or when a job is
 * finished
 */
public class Worker extends Thread {

  boolean shutdown = false;
  boolean running = false;
  boolean fistJobInsert = false;

  public synchronized void run() {
    Configuration.logger.log("Start");
    try {
      this.startThreads();
      while (true) {
        try {
          this.running = false;
          wait(Constants.workerSleepInMillis);
          this.running = true;

          if (!checkWhetherSchedulerHaveEnoughNodes()) {
            Configuration.logger.log("Not Enough Nodes!");
            break;
          }

          // Run Strategy
          Configuration.strategy.run();

          // Shutdown thread condition
          if (shutdown
              && Resources.workingNodes.isEmpty()
              && Resources.allNodes.size() == Resources.openNodes.size()) break;
        } catch (Exception e) {
          Configuration.logger.log("Something went wrong in Strategy");
          Configuration.logger.log(e.toString());
        }
      }
      this.stopThreads();
    } catch (Exception e) {
      Configuration.logger.log("Something went wrong in Strategy");
      Configuration.logger.log(e.toString());
    }
    Configuration.logger.log("End");
  }

  private boolean checkWhetherSchedulerHaveEnoughNodes() {
    if (!this.fistJobInsert && Resources.openNewJobs.isEmpty()) {
      this.fistJobInsert = true;
    }
    // returns true if there are more or equals nodes than required
    return !(Configuration.CONFIG_SCHEDULER_MIN_NODES.get() > 0
        && Configuration.CONFIG_SCHEDULER_MIN_NODES.get() > Resources.allNodes.size()
        && this.fistJobInsert);
  }

  /** wake up Thread */
  public void wakeUpThread() {
    Configuration.logger.log("wake up");

    if (!this.running) {
      this.wakeUp();
    }
  }

  private synchronized void wakeUp() {
    notify();
  }

  /** Start threads */
  private void startThreads() {
    Configuration.jobObserver = new JobObserver();
    Configuration.jobExec = new JobExec();
    Configuration.terminator = new JobTerminator();
    Configuration.jobObserver.start();
    Configuration.jobExec.start();
    Configuration.terminator.start();
  }

  /** Stop threads */
  private void stopThreads() throws InterruptedException {
    Configuration.jobObserver.deactivate();
    Configuration.jobExec.deactivate();
    Configuration.terminator.deactivate();
    Configuration.socketReceiver.deactivate();
    Configuration.jobObserver.join();
    Configuration.jobExec.join();
    Configuration.terminator.join();
    Configuration.socketReceiver.join();
  }

  public void shutdown() {
    this.shutdown = true;
  }
}

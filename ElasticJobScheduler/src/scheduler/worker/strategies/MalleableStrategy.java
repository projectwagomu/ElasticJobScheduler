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

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

public class MalleableStrategy extends SchedulingStrategy {

  final List<Job> jobs = new LinkedList<>();
  final List<DataSheet> runningJobsTime = new ArrayList<>();
  Job head = null;
  long possibleStartTime = 0;
  private boolean changeJobTimeList = false;

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

    this.checkForNewJobs();

    if (jobs.isEmpty() && Resources.openNodes.isEmpty()) return;

    if (!jobs.isEmpty()) {
      Iterator<Job> iter = this.jobs.iterator();

      // Insert Jobs
      while (iter.hasNext()) {
        if (Resources.openNodes.isEmpty()) break;
        Job job = iter.next();
        if (job.getMinNode() <= Resources.openNodes.size()) {
          if (this.head == null || this.head == job) {
            this.execJob(job, iter);
          } else if ((System.currentTimeMillis() + job.requiredTime) < this.possibleStartTime) {
            this.execJob(job, iter);
          }
        } else {
          if (this.head == null) {
            this.head = job;
            break;
          }
        }

        // Calc possible start for head
        boolean setTime = false;
        if (this.head == job && this.changeJobTimeList) {
          Configuration.logger.log("Calculate possible start");
          StringBuilder str = new StringBuilder();
          int countNodes = Resources.openNodes.size();
          for (DataSheet ds : this.runningJobsTime) {
            if (ds == null || ds.job == null || ds.job.activeNodes == null) continue;
            int activeNodes = 0;
            try {
              activeNodes = ds.job.activeNodes.size();
            } catch (Exception ignored) {
            }

            str.append(ds).append(", ");
            countNodes += activeNodes;
            if (countNodes >= this.head.getMinNode() && !setTime) {
              this.possibleStartTime = ds.finish;
              setTime = true;
            }
          }
          Configuration.logger.log(
              "Waiting JobID:"
                  + this.head.getId()
                  + ", possible Start: "
                  + this.possibleStartTime
                  + ", size: "
                  + this.head.getMinNode());
          Configuration.logger.log(
              "Open Node: " + Resources.openNodes.size() + " >> running Jobs: " + str);
          this.changeJobTimeList = false;
        }
      }
    }

    try {
      this.shrinkAndStartNewJobs();
      this.grow();
    } catch (InterruptedException ignored) {
    }

    // Check if the job is running after finish is overdue
    this.runningJobsTime.removeIf(
        dt -> {
          if (dt == null) return true;
          if (dt.finish < System.currentTimeMillis()) {
            return !dt.job.isActive();
          }
          if (dt.job.process == null) return false;
          else return !dt.job.isActive();
        });
  }

  private void shrinkAndStartNewJobs() throws InterruptedException {
    if (!this.jobs.isEmpty()) {
      Configuration.logger.log("Possible Shrink");
      Resources.mutex.acquire();
      LinkedList<Job> copyRunningJobs = new LinkedList<>(Resources.runningJobs);
      Resources.mutex.release();
      copyRunningJobs.sort(Comparator.comparingInt(Job::possibleShrink));
      copyRunningJobs.removeIf(a -> a.possibleShrink() == 0);
      copyRunningJobs.removeIf(a -> !a.isMalleable() || !a.isActive());
      Collections.reverse(copyRunningJobs);

      int maxShrinkNodes = 0;
      for (Job jobR : copyRunningJobs) {
        maxShrinkNodes += jobR.possibleShrink();
      }

      Iterator<Job> iter = this.jobs.iterator();
      // Difference to min_agree from Fabian:
      // We only consider the head of the job queue
      // With the while-loop it should work like
      // Fabian's algorithm, but it is currently not tested
      // while (iter.hasNext()) {
      Job job = iter.next();
      int minNodes = job.getMinNode();
      int openNodes = Resources.openNodes.size();
      int needNodes = minNodes - openNodes;

      if (copyRunningJobs.isEmpty() || maxShrinkNodes == 0) return;
      // with the while-loop: change below return to continue
      if (!(needNodes <= maxShrinkNodes)) return;

      Iterator<Job> iterR = copyRunningJobs.iterator();
      while (iterR.hasNext()) {
        Job jobR = iterR.next();
        if (!jobR.isMalleable() || !jobR.isActive()) {
          iterR.remove();
          continue;
        }
        if (needNodes >= jobR.possibleShrink()) {
          jobR.shrink(jobR.possibleShrink());
          iterR.remove();
          maxShrinkNodes -= jobR.possibleShrink();
        } else if (needNodes < jobR.possibleShrink()) {
          jobR.shrink(needNodes);
          iterR.remove();
          maxShrinkNodes -= needNodes;
        }
      }

      if (Resources.openNodes.size() >= job.getMinNode()) {
        execJob(job, iter);
      }
    }
    // End of while loop
    // }
  }

  public void execJob(Job job, Iterator<Job> iter) {
    try {
      List<Node> nodes = new LinkedList<>();
      while (nodes.size() < job.getMaxNode() && !Resources.openNodes.isEmpty()) {
        Node node = Resources.openNodes.take();
        nodes.add(node);
      }
      // Calc head
      this.runningJobsTime.add(new DataSheet(job));
      this.runningJobsTime.sort((a, b) -> (int) (a.finish - b.finish));
      this.changeJobTimeList = true;

      // Start job
      Configuration.jobExec.add(job, nodes, Constants.JOB_EXEC);
      iter.remove();
      if (this.head == job) {
        this.head = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void grow() throws InterruptedException {
    if (!Resources.openNodes.isEmpty()) {
      Configuration.logger.log("Possible Grow");
      Resources.mutex.acquire();
      LinkedList<Job> tmp = new LinkedList<>(Resources.runningJobs);
      Resources.mutex.release();
      tmp.sort(Comparator.comparingInt(Job::possibleExpand));
      Collections.reverse(tmp);

      for (Job job : tmp) {
        if (job.isMalleable() && job.possibleExpand() > 0) {
          try {
            int possibleExpand = job.possibleExpand();
            List<Node> nodes = new ArrayList<>();
            while (possibleExpand != 0 && !Resources.openNodes.isEmpty()) {
              nodes.add(Resources.openNodes.take());
              possibleExpand--;
            }
            if (!nodes.isEmpty()) {
              Configuration.logger.log(
                  "Possible expand JobID:" + job.getId() + " >> size=" + job.possibleExpand());
              job.expand(nodes);
            }
          } catch (InterruptedException ignored) {
          }
        }
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

  private static class DataSheet {
    final Job job;
    final long finish;

    DataSheet(Job job) {
      this.job = job;
      this.finish = System.currentTimeMillis() + job.requiredTime;
    }

    @Override
    public String toString() {
      String id = this.job.toString();
      int size = 0;
      try {
        size = this.job.activeNodes.size();
      } catch (Exception ignored) {
      }

      return "{ "
          + "Job: "
          + id
          + ", "
          + " size: "
          + size
          + ", "
          + " finish: "
          + this.finish
          + " }";
    }
  }
}

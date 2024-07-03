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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

// This class extends the EasyBackfilling with evolving capabilites
// (Currently a lot of code duplication)
public class EvolvingEasyBackfilling extends SchedulingStrategy {

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

    // process all grow requests
    Job jobToGrow;
    LinkedList<Job> jobsForLater = new LinkedList<>();
    while (!Resources.openNodes.isEmpty() && !Resources.evolvingGrowRequests.isEmpty()) {
      try {
        jobToGrow = Resources.evolvingGrowRequests.take();
        Configuration.logger.log("Possible Grow");
        if (jobToGrow.isEvolving() && jobToGrow.possibleExpand() > 0) {
          try {
            List<Node> nodes = new ArrayList<>();
            nodes.add(Resources.openNodes.take());
            if (!nodes.isEmpty()) {
              Configuration.logger.log(
                  "Possible expand JobID:"
                      + jobToGrow.getId()
                      + " >> size="
                      + jobToGrow.activeNodes.size()
                      + " plus one");
              jobToGrow.expand(nodes);
            }
          } catch (InterruptedException ignored) {
          }
        } else {
          if (jobToGrow.isActive()) {
            jobsForLater.add(jobToGrow);
          }
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    Resources.evolvingGrowRequests.addAll(jobsForLater);

    this.checkForNewJobs();

    this.checkForNewJobs();

    if (jobs.isEmpty() || Resources.openNodes.isEmpty()) return;

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

        this.runningJobsTime.removeIf(
            dt -> {
              if (dt == null) return true;
              if (dt.finish < System.currentTimeMillis()) {
                return !dt.job.isActive();
              }
              if (dt.job.process == null) return false;
              else return !dt.job.isActive();
            });

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
            "Open Node: "
                + Resources.openNodes.size()
                + " >> running Jobs size: "
                + this.runningJobsTime.size()
                + " >> running Jobs: "
                + str);
        this.changeJobTimeList = false;
      }
    }

    // Check if the job is running after finish is overdue
    this.runningJobsTime.removeIf(
        dt -> {
          if (dt.finish < System.currentTimeMillis()) {
            return !dt.job.isActive();
          }
          if (dt.job.process == null) return false;
          else return !dt.job.isActive();
        });
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
          + " JobID:"
          + id
          + ", "
          + " size: "
          + size
          + ", "
          + " finish: "
          + this.finish
          + ", "
          + " }";
    }
  }
}

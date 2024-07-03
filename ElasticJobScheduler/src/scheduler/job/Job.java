/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.job;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import scheduler.Constants;
import scheduler.worker.cluster.Node;

/** Each new job must inherit from this class */
public abstract class Job {
  private static long exitingJobs = 0;
  public final long id;
  public final Path scriptPath;
  public final Path scriptDir;
  public String jobName = "job";
  public long requiredTime = Constants.DEFAULT_REQUIRED_TIME_IN_MILLIS;
  public long timestampJobStart;
  public Process process;
  public List<Node> activeNodes;
  public File outFile;
  public File errFile;

  public Job(Path scriptPath) {
    this.id = ++exitingJobs;
    this.scriptPath = scriptPath;
    this.scriptDir = scriptPath.getParent();
  }

  /**
   * Executing method to start the job Params: nodes - list of open nodes Return: true if the job
   * starts without an error
   */
  public abstract boolean exec(List<Node> nodes);

  /**
   * To filter the data from the file (maxNode, minNode, etc.) Return: true if all data are parse
   */
  public abstract boolean parse();

  /**
   * Specifies whether the program should expand Params: nodes - list of new open nodes to expand
   */
  public abstract boolean expand(List<Node> nodes);

  /**
   * Specifies whether the program should shrink Params: nodes - list of new open nodes to shrink
   */
  public abstract boolean shrink(int size);

  /** Return: number of nodes that the job can still expand */
  public abstract int possibleExpand();

  /** Return: number of nodes that the job can still shrink */
  public abstract int possibleShrink();

  /** Return: number of nodes which are in use */
  public abstract int activeNodes();

  public long getId() {
    return this.id;
  }

  /** Return: number of maximal nodes a job can have */
  public abstract int getMaxNode();

  /** Return: number of minimum nodes a job can have */
  public abstract int getMinNode();

  /** Check if a job is still running */
  public abstract boolean isActive();

  /** Return: active nodes */
  public abstract List<Node> getNodes();

  /** Return: if a job is malleable */
  public abstract boolean isMalleable();

  /** Return: if a job is evolving */
  public abstract boolean isEvolving();

  public String getJobName() {
    return this.jobName;
  }

  public abstract String toString();

  public abstract void isNowReachable();

  public abstract void isNowReachableError();

  public abstract void setGrowShrink();

  public void setTimestampByStart() {
    this.timestampJobStart = System.currentTimeMillis();
  }

  public boolean isJobOverdue() {
    return System.currentTimeMillis() > (this.timestampJobStart + this.requiredTime);
  }

  public abstract void cleanNodes();
}

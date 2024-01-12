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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.jobType.APGASController;
import scheduler.worker.cluster.Node;

public class JobSh extends Job {
  public boolean isNowReachable = false;
  private String jobType;
  private String jobClass;
  private long cdGrowShrink = 0;
  private int minNodes;
  private int maxNodes;

  public JobSh(Path scriptPath) {
    super(scriptPath);
    Configuration.logger.log("JobID:" + this.id + " :: Created sh-File");
  }

  @Override
  public boolean exec(List<Node> nodes) {
    StringBuilder str = new StringBuilder();
    for (Node node : nodes) {
      str.append(node.getId()).append(", ");
    }
    this.activeNodes = nodes;
    Configuration.logger.log("JobID:" + this.id + " :: Start Job on >> " + str);

    try {
      String subPath =
          Constants.SYS_OUT_PATH
              + Configuration.CONFIG_SCHEDULER_ID.get()
              + "/"
              + this.getJobName();
      this.outFile = new File(subPath + ".out." + this.getId());
      this.errFile = new File(subPath + ".err." + this.getId());
      this.outFile.createNewFile();
      this.errFile.createNewFile();
    } catch (IOException e) {
      Configuration.logger.log("Cant create File");
    }

    if (Constants.JOB_TYPE_APGAS.equals(this.jobType)) {
      this.process = APGASController.exec(this, nodes);
    }
    if (this.process != null) this.setTimestampByStart();

    return this.process != null;
  }

  @Override
  public boolean expand(List<Node> nodes) {
    if (nodes.isEmpty()) return false;
    if (this.activeNodes.get(0).getIp() == null) return false;

    if (Constants.JOB_TYPE_APGAS.equals(this.jobType)) {
      return APGASController.grow(this, nodes);
    }
    return false;
  }

  @Override
  public int possibleExpand() {
    if (this.activeNodes == null) return 0;
    if (!this.isActive()) return 0;
    if (!this.isMalleable()) return 0;
    return this.maxNodes - this.activeNodes();
  }

  @Override
  public boolean shrink(int size) {
    if (this.activeNodes.get(0).getIp() == null || size == 0) return false;
    if ((this.activeNodes.size() - size) < this.minNodes) return false;

    if (Constants.JOB_TYPE_APGAS.equals(this.jobType)) {
      return APGASController.shrink(this, size);
    }

    return false;
  }

  @Override
  public int possibleShrink() {
    if (this.activeNodes == null) return 0;
    if (!this.isActive()) return 0;
    if (!this.isMalleable()) return 0;
    return this.activeNodes() - this.minNodes;
  }

  @Override
  public int getMinNode() {
    return this.minNodes;
  }

  @Override
  public int getMaxNode() {
    return this.maxNodes;
  }

  @Override
  public boolean isActive() {
    return this.process != null && this.process.isAlive();
  }

  @Override
  public void isNowReachable() {
    Configuration.logger.log(
        "JobID:" + this.getId() + " :: is now Reachable >> Job is now Malleable");
    this.isNowReachable = true;
  }

  @Override
  public void isNowReachableError() {
    Configuration.logger.log(
        "JobID:" + this.getId() + " :: reachable Error >> Job is now not longer Malleable");
    this.isNowReachable = false;
  }

  @Override
  public void setGrowShrink() {
    this.cdGrowShrink = System.currentTimeMillis();
  }

  @Override
  public int activeNodes() {
    if (this.activeNodes == null) return 0;
    return this.activeNodes.size();
  }

  @Override
  public boolean parse() {
    try (BufferedReader reader = new BufferedReader(new FileReader(this.scriptPath.toString()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        this.setData(line);
      }
      if (Constants.JOB_CLASS_RIGID.equals(this.jobClass)) {
        if (this.maxNodes > 0) this.minNodes = this.maxNodes;
        if (this.maxNodes == 0 && this.minNodes > 0) this.maxNodes = this.minNodes;
      }
      if (this.minNodes == 0) {
        this.minNodes = 1;
      }
      if (this.maxNodes < this.minNodes) {
        this.maxNodes = this.minNodes;
      }
    } catch (IOException e) {
      Configuration.logger.log(
          "JobID:" + this.getId() + " :: Path not found >> " + this.scriptPath);
      return false;
    }
    return this.minNodes <= Resources.allNodes.size();
  }

  /** Set data from job-file */
  public void setData(String line) {
    if (line == null) return;
    if (!line.startsWith(Constants.HASHTAG)) return;
    line = line.replace(Constants.HASHTAG, "");
    if (line.contains(Constants.JOB_TYPE)) {
      line = line.replace(Constants.JOB_TYPE, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.JOB_TYPE + ": " + line);
      this.jobType = line;
    } else if (line.contains(Constants.JOB_CLASS)) {
      line = line.replace(Constants.JOB_CLASS, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.JOB_CLASS + ": " + line);
      this.jobClass = line;
    } else if (line.contains(Constants.MIN_NODES)) {
      line = line.replace(Constants.MIN_NODES, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.MIN_NODES + ": " + line);
      this.minNodes = Integer.parseInt(line);
    } else if (line.contains(Constants.MAX_NODES)) {
      line = line.replace(Constants.MAX_NODES, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.MAX_NODES + ": " + line);
      this.maxNodes = Integer.parseInt(line);
    } else if (line.contains(Constants.NODES)) {
      line = line.replace(Constants.NODES, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.NODES + ": " + line);
      this.minNodes = Integer.parseInt(line);
      this.maxNodes = Integer.parseInt(line);
    } else if (line.contains(Constants.JOB_NAME)) {
      line = line.replace(Constants.JOB_NAME, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.JOB_NAME + ": " + line);
      this.jobName = line;
    } else if (line.contains(Constants.REQUIRED_TIME)) {
      line = line.replace(Constants.REQUIRED_TIME, "").trim();
      Configuration.logger.log(
          "JobID:" + this.getId() + " >> set " + Constants.REQUIRED_TIME + ": " + line);
      this.requiredTime = Long.parseLong(line);
    }
  }

  @Override
  public String toString() {
    String str = "{";
    str += " JobID:" + this.id + ", ";
    str += " JobType:" + this.jobType + ", ";
    str += " JobClass:" + this.jobClass + ", ";
    str += " MinNodes:" + this.minNodes + ", ";
    str += " MaxNodes:" + this.maxNodes + " ";
    str += "}";
    return str;
  }

  public List<Node> getNodes() {
    return this.activeNodes;
  }

  public boolean isMalleable() {
    if (this.jobClass == null) return false;
    if (!this.isNowReachable) return false;
    if ((System.currentTimeMillis() - (this.cdGrowShrink + Constants.CD_GROW_SHRINK)) <= 0)
      return false;
    return Constants.JOB_CLASS_MALLEABLE.equals(this.jobClass);
  }

  public void cleanNodes() {
    if (Constants.JOB_TYPE_APGAS.equals(this.jobType)) {
      APGASController.kill(this);
    }
  }
}

/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.job.jobType;

import static java.lang.Thread.sleep;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.job.JobSh;
import scheduler.worker.cluster.Node;

/** Functions that are for APGAS */
public class APGASController {

  static final String nodeFileName = Constants.APGAS_NODE_FILE_NAME;

  /** Start APGAS on target node */
  public static Process exec(Job job, List<Node> nodes) {
    Process process = null;
    Configuration.logger.log("JobID:" + job.getId() + " :: create nodeFile");
    File file =
        new File(
            Constants.SYS_OUT_PATH
                + Configuration.CONFIG_SCHEDULER_ID.get()
                + "/"
                + job.getJobName()
                + "."
                + APGASController.nodeFileName
                + "."
                + job.getId());
    try {
      if (!file.exists()) {
        file.createNewFile();
      }
      PrintWriter pw = new PrintWriter(file);
      nodes.forEach(node -> pw.println(node.getId()));
      pw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      ProcessBuilder processBuilder = Configuration.launcher.launch(job, nodes, file);
      processBuilder.redirectOutput(job.outFile);
      processBuilder.redirectError(job.errFile);
      process = processBuilder.start();
      Configuration.logger.log(
          "JobID:" + job.getId() + " :: start job on host >> " + job.activeNodes.get(0).getId());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return process;
  }

  /** grow job */
  public static boolean grow(Job job, List<Node> nodes) {
    StringBuilder nodesString = new StringBuilder();
    for (Node node : nodes) {
      nodesString.append(node.getId()).append(" ");
    }

    try {
      Socket socket = new Socket(job.activeNodes.get(0).getIp(), Constants.SCHEDULER_PORT);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      out.println("grow " + nodes.size() + " " + nodesString);

      for (Node node : nodes) {
        Configuration.logger.log("JobID:" + job.getId() + " :: Expand on:" + node.getId());
        node.setJob(job);
        job.activeNodes.add(node);
        Resources.workingNodes.add(node);
      }
      socket.close();
    } catch (Exception e) {
      Resources.openNodes.addAll(nodes);
      return false;
    } finally {
      job.setGrowShrink();
    }
    return true;
  }

  /** shrink job */
  public static boolean shrink(Job job, int size) {
    try {
      Socket socket = new Socket(job.activeNodes.get(0).getIp(), Constants.SCHEDULER_PORT);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      out.println("shrink " + size);

      List<Node> tmpNodeList = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        socket.setSoTimeout(Constants.TIMEOUT_TIME_SOCKET_MILLIS);
        final String line = reader.readLine();
        if (Resources.nodeById != null && line != null && Resources.nodeById.containsKey(line)) {
          Node node = Resources.nodeById.get(line);
          job.activeNodes.remove(node);
          node.setJob(null);
          Resources.workingNodes.remove(node);
          tmpNodeList.add(node);
        }
      }
      kill(tmpNodeList, job, false);
      for (Node node : tmpNodeList) {
        Configuration.logger.log("JobID:" + job.getId() + " :: free Node:" + node.getId());
        try {
          Resources.openNodes.put(node);
        } catch (InterruptedException e) {
          // node could not be put into openNode
          // should never be happening
          e.printStackTrace();
          return false;
        }
      }
      socket.close();
    } catch (IOException e) {
      Configuration.logger.log("JobID:" + job.getId() + " :: Error by shrink");
      job.isNowReachableError();
      return false;
    } finally {
      job.setGrowShrink();
    }
    return true;
  }

  public static void kill(JobSh job) {
    List<Node> nodes = job.getNodes();
    kill(nodes, job, true);
  }

  public static void kill(List<Node> nodes, Job job, boolean destroyProcess) {
    List<Process> isClosing = new LinkedList<>();
    for (Node node : nodes) {
      Configuration.logger.log("JobID:" + job.getId() + " :: clear node:" + node.getId());
      String[] execString = {"ssh", node.getId(), "killall java"};
      try {
        Process process = Runtime.getRuntime().exec(execString);
        isClosing.add(process);
        Process process1 = Runtime.getRuntime().exec(execString);
        isClosing.add(process1);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    while (!isClosing.isEmpty()) {
      isClosing.removeIf(pc -> !pc.isAlive());
      try {
        sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    if (job.isJobOverdue()) {
      try {
        FileWriter fwOut = new FileWriter(job.outFile, true);
        FileWriter fwErr = new FileWriter(job.errFile, true);
        fwOut.write("**** JobID:" + job.getId() + " :: was Overdue ****\n");
        fwErr.write("**** JobID:" + job.getId() + " :: was Overdue ****\n");
        fwOut.close();
        fwErr.close();
      } catch (Exception e) {
        Configuration.logger.log("Cant write Job Overdue");
      }
    }

    if (destroyProcess) {
      if (job.isActive()) job.process.destroy();
    }
  }
}

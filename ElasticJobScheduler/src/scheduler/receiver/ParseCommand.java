/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.receiver;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;
import scheduler.job.JobSh;
import scheduler.job.jobType.APGASController;
import scheduler.worker.cluster.Node;

/** Manages all incoming messages from the receiver */
public class ParseCommand {

  public static boolean message(String command, PrintWriter response) {
    Pattern patternShFile = Pattern.compile(".sh$", Pattern.CASE_INSENSITIVE);
    Pattern patternNewNodes =
        Pattern.compile("^" + Constants.COMMAND_ADD_NODE + " .*$", Pattern.CASE_INSENSITIVE);

    if (patternShFile.matcher(command).find()) {
      JobSh jobSh = new JobSh(Path.of(command));
      Resources.openNewJobs.add(jobSh);
      Configuration.worker.wakeUpThread();
      response.println("Add Job >> JobID:" + jobSh.getId());
    } else if (patternNewNodes.matcher(command).find()) {
      String[] newNodes = Node.addNewNodesAsString(command);
      response.println(ParseCommand.generateStringFromArray("Add Nodes", newNodes));
    } else if (Constants.COMMAND_SHUTDOWN_PROGRAMM.equals(command)) {
      response.println("Start >> " + Constants.COMMAND_SHUTDOWN_PROGRAMM);
      Configuration.worker.shutdown();
      return false;
    } else if (command.contains("APGAS-Ready")) {
      String ip = command.replace("APGAS-Ready:", "");
      Node node = Resources.nodeByIP.get(ip);
      Configuration.logger.log("Start Connection To APGAS >> ip:" + ip + " node:" + node);
      if (node != null && node.hasJob()) {
        node.getJob().isNowReachable();
        Configuration.worker.wakeUpThread();
      }
    } else if (command.contains("Time")) {
      /*
       * possible values:
       * - growTimeAPGAS
       * - shrinkTimeAPGAS
       * - postGrowTimeGLB
       * - postShrinkTimeGLB
       * - preGrowTimeGLB
       * - preShrinkTimeGLB
       */
      String message = command.split(":")[0];
      String ip = command.split(":")[1];

      Node node = Resources.nodeByIP.get(ip);

      String type = message.split(";")[0];
      String nbPlaces = message.split(";")[1];
      String shrinkTime = message.split(";")[2];

      Configuration.logger.log(
          "JobID:"
              + node.getJob().getId()
              + " "
              + node.getJob().getJobName()
              + " :: "
              + type
              + " : "
              + nbPlaces
              + " : "
              + shrinkTime
              + " : "
              + (Long.parseLong(shrinkTime) / 1e9)
              + " sec");
    } else if (command.contains("Evolving")) {
      /*
       * possible values:
       * - Request
       * - Release
       * Always one node is requested/released
       */

      // We start with only released, i.e., requested is always rejected
      // Evolving;Release;node

      String message = command.split(":")[0];
      String ip = command.split(":")[1];
      Job job = Resources.nodeByIP.get(ip).getJob();
      String type = message.split(";")[1];
      String nodeString = message.split(";")[2];

      Configuration.logger.log(
          "JobID:"
              + job.getId()
              + " "
              + job.getJobName()
              + " :: "
              + "Evolving"
              + " : "
              + type
              + " : "
              + nodeString);

      if ("Release".equals(type)) {
        Resources.evolvingGrowRequests.remove(job);
        Node node = Resources.nodeById.get(nodeString);
        job.activeNodes.remove(node);
        job.setGrowShrink();
        node.setJob(null);
        // We assume only having APGAS jobs here
        Resources.workingNodes.remove(node);
        ArrayList<Node> nodeArrayList = new ArrayList<>();
        nodeArrayList.add(node);
        APGASController.kill(nodeArrayList, job, false);
        Configuration.logger.log(
            "JobID:" + job.getId() + " :: free Node caused by Release:" + node.getId());
        try {
          Resources.openNodes.put(node);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

      } else if ("Request".equals(type)) {
        Configuration.logger.log("Received a Grow Request from JobID:" + job.getId());
        if (!Resources.evolvingGrowRequests.contains(job)) {
          Resources.evolvingGrowRequests.add(job);
          Configuration.logger.log("Added a Grow Request from JobID:" + job.getId());
        }

      } else {
        response.println("Undefined Evolving Message");
      }

      Configuration.worker.wakeUpThread();
      // END Evolving
    } else {
      response.println("undefined Message");
    }

    return true;
  }

  private static String generateStringFromArray(String headerMsg, String[] strArray) {
    StringBuilder res = new StringBuilder(headerMsg + " >> ");
    for (int i = 0; i < strArray.length; i++) {
      if (i < strArray.length - 1) res.append(strArray[i]).append(",");
      if (i == strArray.length - 1) res.append(strArray[i]);
    }
    return res.toString();
  }
}

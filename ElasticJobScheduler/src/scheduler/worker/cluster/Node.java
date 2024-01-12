/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.worker.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import scheduler.Configuration;
import scheduler.Constants;
import scheduler.Resources;
import scheduler.job.Job;

/** Represents a computer on the cluster */
public class Node {
  final String id;
  String ip;

  Job job = null;

  boolean active = true;

  public Node(String id, String ip) {
    this.id = id;
    this.ip = ip;
  }

  public static String[] addNewNodesAsString(String nodesListAsString) {
    String[] newNodes = stringToArray(nodesListAsString);
    setNewNodes(newNodes);
    return newNodes;
  }

  private static String[] stringToArray(String stringArray) {
    if (stringArray == null) return new String[] {};
    if (stringArray.contains(Constants.COMMAND_ADD_NODE)) {
      stringArray = stringArray.replace(Constants.COMMAND_ADD_NODE, "");
    }
    if (stringArray.contains(Constants.COMMAND_DELETE_NODE)) {
      stringArray = stringArray.replace(Constants.COMMAND_DELETE_NODE, "");
    }
    stringArray = stringArray.replace(" ", "");
    if (stringArray.contains(",")) {
      return stringArray.split(",");
    } else {
      return new String[] {stringArray};
    }
  }

  /** checks whether a node can be reached and gets the ip address */
  public static void setNewNodes(String[] nodes) {
    for (String nodeId : nodes) {
      if (!Resources.nodeById.containsKey(nodeId)) {
        try {
          InetAddress inet = InetAddress.getByName(nodeId);

          boolean success = false;
          for (int attempt = 1; attempt <= Constants.NEW_NODES_ATTEMPTS; attempt++) {
            if (inet.isReachable(100)) {
              Node node = new Node(nodeId, inet.getHostAddress());
              Resources.nodeById.put(nodeId, node);
              Resources.nodeByIP.put(node.getIp(), node);
              Resources.allNodes.add(node);
              Resources.openNodes.add(node);
              Configuration.logger.log("Add Node >> " + node);
              success = true;
              break;
            } else {
              Configuration.logger.log("Node is not reachable, try again: " + nodeId);
            }

            if (attempt < Constants.NEW_NODES_ATTEMPTS) {
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          }

          if (!success) {
            Configuration.logger.log("Error: Node is not reachable, try again: " + nodeId);
          }
        } catch (UnknownHostException e) {
          Configuration.logger.log("Error: Name or service not known: " + nodeId);
        } catch (IOException e) {
          Configuration.logger.log("Error: Node is not reachable: " + nodeId);
        }
      } else {
        Node node = Resources.nodeById.get(nodeId);
        Configuration.logger.log("Error: NodeID:" + nodeId + " exists >> " + node);
      }
    }
  }

  public void deactivateNode() {
    Configuration.logger.log("Deactivate Node >> " + this);
    this.active = false;
  }

  public String toString() {
    String str = "{";
    str += " JobID:" + this.id + " , ";
    str += " active: " + this.active + " , ";
    str += " job: " + this.job + " , ";
    str += " ip: " + this.ip + " ";
    str += "}";
    return str;
  }

  public boolean isActive() {
    return this.active;
  }

  public boolean hasJob() {
    return this.job != null;
  }

  public String getId() {
    return this.id;
  }

  public String getIp() {
    return this.ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public Job getJob() {
    return this.job;
  }

  public void setJob(Job job) {
    this.job = job;
  }
}

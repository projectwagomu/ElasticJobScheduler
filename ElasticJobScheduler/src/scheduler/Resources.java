/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

public class Resources {
  /** Jobs */
  public static final Semaphore mutex = new Semaphore(1);

  public static final LinkedBlockingQueue<Job> openNewJobs = new LinkedBlockingQueue<>();
  public static final LinkedBlockingQueue<Job> runningJobs = new LinkedBlockingQueue<>();

  /** Nodes */
  public static final List<Node> allNodes = new ArrayList<>();

  public static final List<Node> workingNodes = new ArrayList<>();
  public static final LinkedBlockingQueue<Node> openNodes = new LinkedBlockingQueue<>();
  public static final Map<String, Node> nodeById = new Hashtable<>();
  public static final Map<String, Node> nodeByIP = new Hashtable<>();

  /** Scheduler */
  public static String SCHEDULER_IP;
}

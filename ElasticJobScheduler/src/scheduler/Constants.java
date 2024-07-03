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

public class Constants {
  /** SYSTEM */
  public static final String SYS_OUT_PATH = "../../out/";

  /** APGASController */
  public static final String APGAS_NODE_FILE_NAME = "nodeFile";

  /** JobSH */
  public static final String JOB_TYPE = "JOB_TYPE";

  public static final String JOB_TYPE_APGAS = "apgas";
  public static final String JOB_CLASS = "JOB_CLASS";
  public static final String JOB_CLASS_RIGID = "rigid";
  public static final String JOB_CLASS_MALLEABLE = "malleable";
  public static final String JOB_CLASS_EVOLVING = "evolving";
  public static final String MIN_NODES = "MIN_NODES";
  public static final String MAX_NODES = "MAX_NODES";
  public static final String JOB_NAME = "JOB_NAME";
  public static final String REQUIRED_TIME = "REQUIRED_TIME";
  public static final String NODES = "NODES";
  public static final String HASHTAG = "#";
  public static final long DEFAULT_REQUIRED_TIME_IN_MILLIS = 1000 * 60;
  public static final long CD_GROW_SHRINK = 1000 * 60;
  public static final int TIMEOUT_TIME_SOCKET_MILLIS = 1000 * 20;
  public static final int NEW_NODES_ATTEMPTS = 5;
  public static final String COMMAND_SHUTDOWN_PROGRAMM = "shutdown";
  public static final String COMMAND_ADD_NODE = "Add Node";
  public static final String COMMAND_DELETE_NODE = "Delete Node";

  /** JobExec */
  public static final String JOB_EXEC = "exec";

  /** Socket */
  public static final int SCHEDULER_PORT = 8081;

  /** Worker */
  public static final int workerSleepInMillis = 5000;

  /** JobObserver */
  public static final int jobObserverSleepInMillis = 100;
}

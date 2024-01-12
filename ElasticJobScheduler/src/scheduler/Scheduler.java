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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import scheduler.job.launcher.Launcher;
import scheduler.logger.Logger;
import scheduler.receiver.SocketReceiver;
import scheduler.worker.Worker;
import scheduler.worker.strategies.SchedulingStrategy;

public class Scheduler {
  public static void main(String[] args) throws IOException {
    // set Scheduler Strategy and Launcher
    if (setStrategy() || setLauncher()) return;

    // set Scheduler IP and ID
    Resources.SCHEDULER_IP = Inet4Address.getLocalHost().getHostAddress();
    setUniqueId();

    // Start Logger
    Logger logger = new Logger();
    Configuration.logger = logger;
    logger.start();
    logger.log("Mode: " + Configuration.CONFIG_SCHEDULER_STRATEGIES.get());

    // Start Socketserver
    SocketReceiver socketReceiver = new SocketReceiver();
    Configuration.socketReceiver = socketReceiver;
    socketReceiver.start();

    // Start Worker
    Worker worker = new Worker();
    Configuration.worker = worker;
    worker.start();
  }

  private static boolean setLauncher() {
    final String className = Configuration.CONFIG_SCHEDULER_JOB_LAUNCHER.get();
    try {
      Configuration.launcher =
          (Launcher) Class.forName("scheduler." + className).getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException e) {
      System.err.println("Unable to instantiate Job Launcher " + className);
      System.err.println(e);
      return true;
    }
    return false;
  }

  private static boolean setStrategy() {
    final String className = Configuration.CONFIG_SCHEDULER_STRATEGIES.get();
    try {
      Configuration.strategy =
          (SchedulingStrategy)
              Class.forName("scheduler.worker.strategies." + className)
                  .getDeclaredConstructor()
                  .newInstance();
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException e) {
      System.err.println("Unable to instantiate SchedulingStrategy " + className);
      System.err.println(e);
      return true;
    }
    return false;
  }

  /**
   * Return scheduler id, if args have "mode=<strategy>", otherwise return
   * System.currentTimeMillis() Generate subdirectory in Out with scheduler id
   */
  private static String setUniqueId() {
    String command = Configuration.CONFIG_SCHEDULER_ID.get();

    if (command == null) {
      command = String.valueOf(System.currentTimeMillis());
      Configuration.CONFIG_SCHEDULER_ID.set(command);
    } else {
      command = command.trim();
    }
    File folder = new File(Constants.SYS_OUT_PATH + command);
    return folder.mkdir() ? command : "";
  }
}

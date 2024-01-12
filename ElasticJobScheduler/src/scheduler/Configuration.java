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

import scheduler.job.launcher.Launcher;
import scheduler.logger.Logger;
import scheduler.receiver.SocketReceiver;
import scheduler.worker.Worker;
import scheduler.worker.jobThreads.JobExec;
import scheduler.worker.jobThreads.JobObserver;
import scheduler.worker.jobThreads.JobTerminator;
import scheduler.worker.strategies.SchedulingStrategy;

public class Configuration<T> {

  /** Properties */
  public static final String SCHEDULER_STRATEGIES = "scheduler.strategies";

  public static final String SCHEDULER_JOB_LAUNCHER = "scheduler.job.launcher";

  public static final String SCHEDULER_INSIDE_JOB_LAUNCHER = "scheduler.inside.job.launcher";

  public static final String SCHEDULER_JOB_WORKERS = "scheduler.job.workers";
  public static final String SCHEDULER_MIN_NODES = "scheduler.min.nodes";
  public static final String SCHEDULER_ID = "scheduler.id";
  public static final Configuration<String> CONFIG_SCHEDULER_STRATEGIES =
      new Configuration<>(SCHEDULER_STRATEGIES, "FCFS", String.class);
  public static final Configuration<String> CONFIG_SCHEDULER_ID =
      new Configuration<>(SCHEDULER_ID, String.class);
  public static final Configuration<String> CONFIG_SCHEDULER_JOB_LAUNCHER =
      new Configuration<>(SCHEDULER_JOB_LAUNCHER, "job.launcher.SshLauncher", String.class);

  public static final Configuration<String> CONFIG_SCHEDULER_INSIDE_JOB_LAUNCHER =
      new Configuration<>(
          SCHEDULER_INSIDE_JOB_LAUNCHER, "apgas.launcher.SshLauncher", String.class);

  public static final Configuration<String> CONFIG_SCHEDULER_JOB_WORKERS =
      new Configuration<>(SCHEDULER_JOB_WORKERS, "4", String.class);

  public static final Configuration<Integer> CONFIG_SCHEDULER_MIN_NODES =
      new Configuration<>(SCHEDULER_MIN_NODES, 0, Integer.class);

  /** All active threads and active strategy */
  public static SocketReceiver socketReceiver = null;

  public static Logger logger = null;
  public static Worker worker = null;
  public static JobObserver jobObserver = null;
  public static JobExec jobExec = null;
  public static JobTerminator terminator = null;
  public static Launcher launcher = null;
  public static SchedulingStrategy strategy = null;
  private final String name;
  private final Class<T> propertyType;
  private T cachedValue;
  private T defaultValue;

  /**
   * Constructor
   *
   * @param name The PropertyName of the Configuration Value
   * @param defaultValue A default Value to use if no one is provided via the System-Properties
   * @param propertyType The Type of the Property-Value
   */
  private Configuration(final String name, final T defaultValue, final Class<T> propertyType) {
    this.name = name;
    this.setDefaultValue(defaultValue);
    this.defaultValue = defaultValue;
    this.propertyType = propertyType;
  }

  /**
   * Constructor
   *
   * @param name The PropertyName of the Configuration Value
   * @param propertyType The Type of the Property-Value
   */
  private Configuration(final String name, final Class<T> propertyType) {
    this.name = name;
    this.propertyType = propertyType;
  }

  /**
   * Retrieve the PropertyValue of the Configuration.
   *
   * <p>This returns the default value if provided, and no other Value was set or retrieved via the
   * System-Properties. If a Value is set via the System-Properties this will override the default
   * Value. If a Value is set via the setter Method, this Value will override the default Value as
   * well as the System-Property Value.
   *
   * @return The Value of this Configuration
   */
  public synchronized T get() {

    if (cachedValue != null) {
      return cachedValue;
    }

    final String value = System.getProperty(name);
    if (value == null) {
      if (defaultValue != null) {
        this.set(defaultValue);
      }
      return defaultValue;
    }

    if (propertyType.equals(Boolean.class)) {
      final Boolean aBoolean = Boolean.valueOf(value);
      cachedValue = (T) aBoolean;
      return cachedValue;
    }

    if (propertyType.equals(Integer.class)) {
      final Integer anInt = Integer.valueOf(value);
      cachedValue = (T) anInt;
      return cachedValue;
    }

    if (propertyType.equals(Double.class)) {
      final Double aDouble = Double.valueOf(value);
      cachedValue = (T) aDouble;
      return cachedValue;
    }

    if (propertyType.equals(String.class)) {
      cachedValue = (T) value;
      return cachedValue;
    }

    return (T) value;
  }

  /**
   * Set the given value as value for this Configuration.
   *
   * @param value The value to set for this Configuration
   */
  public synchronized void set(T value) {
    cachedValue = value;
    System.setProperty(name, String.valueOf(cachedValue));
  }

  /**
   * Set the default value to use if no System-Property is present. This can be overridden by a set
   * call.
   *
   * @param defaultValue The Value to use as default
   */
  public synchronized void setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
  }
}

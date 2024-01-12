/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.LinkedBlockingQueue;
import scheduler.Configuration;
import scheduler.Constants;

/** Documents all outputs from all threads and stores them in subdirectories in out */
public class Logger extends Thread {
  public final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
  private final PrintWriter out;

  public Logger() {
    try {
      FileOutputStream file =
          new FileOutputStream(
              Constants.SYS_OUT_PATH
                  + Configuration.CONFIG_SCHEDULER_ID.get()
                  + "/"
                  + Configuration.CONFIG_SCHEDULER_ID.get()
                  + "_output.txt");
      this.out = new PrintWriter(file, true);
      this.log("Start Tracking");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void run() {
    while (true) {
      try {
        String str = queue.take();

        if (str.endsWith(" - Close Socket")) {
          this.message(str);
          this.log("End Tracking");
          this.message(queue.take());
          break;
        } else {
          this.message(str);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    this.out.close();
  }

  private void message(String str) {
    System.out.println(str);
    this.out.println(str);
  }

  public void log(String str) {
    try {
      final long timeStamp = System.currentTimeMillis();
      final String formattedDateTime =
          LocalDateTime.now()
              .truncatedTo(ChronoUnit.SECONDS)
              .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
      final String className = Thread.currentThread().getStackTrace()[2].getClassName();
      this.queue.put(
          formattedDateTime
              + "; "
              + timeStamp
              + "; "
              + "["
              + className
              + ":"
              + methodName
              + "] - "
              + str);

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

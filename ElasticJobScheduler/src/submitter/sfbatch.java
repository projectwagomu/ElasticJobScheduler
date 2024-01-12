/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package submitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import scheduler.Constants;

public class sfbatch {
  public static void main(String[] args) {
    for (String arg : args) {
      sendMessage(arg);
    }
  }

  public static void sendMessage(String script) {
    try {
      Socket socket = new Socket("localhost", Constants.SCHEDULER_PORT);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

      out.println(script);
      String response = in.readLine();
      System.out.println(response);

      in.close();
      out.close();
      socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

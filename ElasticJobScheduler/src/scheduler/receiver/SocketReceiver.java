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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import scheduler.Configuration;
import scheduler.Constants;

/** Establishes the socket connection and closes it again */
public class SocketReceiver extends Thread {

  private ServerSocket serverSocket;

  public void run() {
    try {
      this.serverSocket = new ServerSocket(Constants.SCHEDULER_PORT);
      while (this.serverSocket != null) {
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String command = in.readLine();

        Configuration.logger.log(command);

        PrintWriter response = new PrintWriter(socket.getOutputStream());

        ParseCommand.message(command, response);
        response.flush();
      }
    } catch (Exception ignored) {

    } finally {
      Configuration.logger.log("Close Socket");
    }
  }

  public void deactivate() {
    if (this.serverSocket != null) {
      try {
        this.serverSocket.close();
        this.serverSocket = null;
      } catch (IOException ignored) {
      }
    }
  }
}

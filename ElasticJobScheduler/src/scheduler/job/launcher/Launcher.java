/*
 * Copyright (c) 2023 Wagomu project.
 *
 * This program and the accompanying materials are made available to you under
 * the terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package scheduler.job.launcher;

import java.io.File;
import java.util.List;
import scheduler.job.Job;
import scheduler.worker.cluster.Node;

public interface Launcher {

  ProcessBuilder launch(Job job, List<Node> nodes, File file);
}

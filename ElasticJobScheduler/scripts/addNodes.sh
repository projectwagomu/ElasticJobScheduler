#!/bin/bash
#
# Copyright (c) 2023 Wagomu project.
#
# This program and the accompanying materials are made available to you under
# the terms of the Eclipse Public License 2.0 which accompanies this distribution,
# and is available at https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#

CWD="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $CWD/../build/classes/

if [ $# -eq 0 ]; then
  echo "No nodes as parameters provided. Exit."
  exit 1
fi

nodes=$(
  IFS=,
  echo "$*"
)

java submitter.sfbatch "Add Node $nodes"

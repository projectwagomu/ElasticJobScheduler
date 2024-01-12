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

if [ $# -ne 1 ]; then
  echo "Exactly one job file as parameter is required."
  exit 1
fi

java submitter.sfbatch "$1"

#Example:
#java submitter.sfbatch ${DIR}/apgas-jobs/UTS_18_malleable_1_4.sh

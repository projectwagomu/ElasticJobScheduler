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

cd $CWD/../..
git clone https://github.com/projectwagomu/apgas.git apgas
cd apgas
git checkout v0.0.2
mvn install -DskipTests

cd $CWD/../..
git clone https://github.com/projectwagomu/lifelineglb.git lifelineglb
cd lifelineglb
git checkout v0.0.2
mvn package

cd $CWD/..
ant clean compile

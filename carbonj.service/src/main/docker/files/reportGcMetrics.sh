#!/bin/bash
#
# Copyright (c) 2018, salesforce.com, inc.
# All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause
# For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
#


# pod id
pod=pod98

# group id
groupId=${DW_GROUP_ID}

# service name
serviceName=${DW_SVC_VERSION}

# app name
appName=${SVC_PROP_APP_NAME}

# host name
hostName=$( hostname )

# namespace
namespace=$pod.$groupId.$appName.$hostName.${serviceName//./_}.jvm.gc.stw.time

# cache host
grafServer=${DW_GRAPHITE_HOST}
grafPort=2003

# gc log file
# java 9+ has new GC logging...
log=$( echo $( ls -t /app/log/gc.log ) | awk '{print $1}' )
#log=$( echo $( ls -t /app/log/gc*current ) | awk '{print $1}' )

#
# do work
#

# current timestamp in seconds
time=$( date +%s )

# grep and sum all times in file for last one minute
# TODO remove hashes
gcTime=$( tail -n 10000 $log | grep "Total time" | sed 's/\[//g'|sed 's/\]/ /g'| awk -v LAST_MINUTE="$(date -ud "-1 minutes" +"%Y-%m-%dT%H:%M:%S")" '($1 " " $2) >= LAST_MINUTE' | grep -Eo "Total time for which application threads were stopped: [0-9]+\.[0-9]+\ " |awk '{ print $9}' > /tmp/gc && awk '{s+=$1} END {print s}' /tmp/gc )

# print to carbon
echo "$namespace $gcTime $time"
echo "$namespace $gcTime $time" | nc -u -v $grafServer $grafPort
exit $?

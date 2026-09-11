#!/bin/sh
#
# Use this script to run your program LOCALLY.
#
# Note: Changing this script WILL NOT affect how CodeCrafters runs your program.
#
# Learn more: https://codecrafters.io/program-interface

set -e # Exit early if any commands fail

# Copied from .codecrafters/compile.sh
#
# - Edit this to change how your program compiles locally
# - Edit .codecrafters/compile.sh to change how your program compiles remotely
(
  cd "$(dirname "$0")" # Ensure compile steps are run within the repository directory
  mvn -q -B package -Ddir=/tmp/codecrafters-build-shell-java
)

# Copied from .codecrafters/run.sh
#
# - Edit this to change how your program runs locally
# - Edit .codecrafters/run.sh to change how your program runs remotely

# Debug: DEBUG=1 ./your_program.sh   -> JVM waits for IntelliJ "Remote JVM Debug" on port 5005
#        DEBUG=1 SUSPEND=n ./your_program.sh  -> don't wait
#        DEBUG=1 DEBUG_PORT=5006 ./your_program.sh  -> use a different port
SUSPEND="${SUSPEND:-y}"
DEBUG_PORT="${DEBUG_PORT:-5005}"

DEBUG_OPTS=""
if [ -n "${DEBUG:-}" ]; then
  DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=${SUSPEND},address=*:${DEBUG_PORT}"
  echo ">> DEBUG=${DEBUG} SUSPEND=${SUSPEND} DEBUG_PORT=${DEBUG_PORT}" >&2
  echo ">> JDWP listening on ${DEBUG_PORT} (suspend=${SUSPEND}) — attach IntelliJ 'Remote JVM Debug'" >&2
fi

exec java --enable-native-access=ALL-UNNAMED --enable-preview ${DEBUG_OPTS} -jar /tmp/codecrafters-build-shell-java/codecrafters-shell.jar "$@"

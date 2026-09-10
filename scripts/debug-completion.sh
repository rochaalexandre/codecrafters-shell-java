#!/usr/bin/env bash
#
# Run the shell in a REAL terminal (so <TAB> reaches JLine) with the JDWP
# debug agent open, then attach IntelliJ.
#
#   IntelliJ: Run > Edit Configurations > + > Remote JVM Debug > port 5005 (name it "attach")
#   Terminal: ./scripts/debug-completion.sh          # JVM waits for the debugger
#   IntelliJ: hit Debug on "attach"                  # program resumes here
#
# Env knobs:
#   SUSPEND=n   ./scripts/debug-completion.sh        # don't wait for a debugger
#   PORT=5006   ./scripts/debug-completion.sh
#   NO_DEBUG=1  ./scripts/debug-completion.sh        # plain run, no agent
set -e

cd "$(dirname "$0")/.."

BUILD_DIR=/tmp/codecrafters-build-shell-java
JAR="$BUILD_DIR/codecrafters-shell.jar"
PORT="${PORT:-5005}"
SUSPEND="${SUSPEND:-y}"

# Fake PATH entries the completion tester uses: xyz_dog / xyz_owl / xyz_rat
FAKE_BIN=/tmp/bee
mkdir -p "$FAKE_BIN"
for n in xyz_dog xyz_owl xyz_rat; do
  printf '#!/bin/sh\necho hi\n' > "$FAKE_BIN/$n"
  chmod +x "$FAKE_BIN/$n"
done

echo ">> building"
mvn -q -B package -Ddir="$BUILD_DIR"

JAVA_OPTS=(--enable-native-access=ALL-UNNAMED --enable-preview)
if [ -z "${NO_DEBUG:-}" ]; then
  JAVA_OPTS+=("-agentlib:jdwp=transport=dt_socket,server=y,suspend=${SUSPEND},address=*:${PORT}")
  echo ">> JDWP listening on ${PORT} (suspend=${SUSPEND}) — attach IntelliJ 'Remote JVM Debug'"
fi

echo ">> PATH prepended with ${FAKE_BIN} (xyz_dog xyz_owl xyz_rat)"
echo ">> type: xyz_<TAB>   then <TAB> again"
echo

exec env PATH="${FAKE_BIN}:${PATH}" java "${JAVA_OPTS[@]}" -jar "$JAR"

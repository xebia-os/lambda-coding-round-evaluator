#!/bin/bash
set -e

VERSION=${1:-1.8}

echo "Installing OpenJDK ${VERSION}..."

export JAVA_HOME=$(echo /tmp/usr/lib/jvm/java-${VERSION}.0-openjdk-${VERSION}*)

if ! [ -d $JAVA_HOME ]; then
  set -x
  curl -sSL https://lambci.s3.amazonaws.com/binaries/java-${VERSION}.0-openjdk-devel.tgz | tar -xz -C /tmp

  # For some reason, libjvm.so needs to be physically present
  # Can't symlink it, have to copy, but everything else can be symlinks
  export JAVA_HOME=$(echo /tmp/usr/lib/jvm/java-${VERSION}.0-openjdk-${VERSION}*)
  cp -as /usr/lib/jvm/java-${VERSION}*/jre $JAVA_HOME/
  rm $JAVA_HOME/jre/lib/amd64/server/libjvm.so
  cp /usr/lib/jvm/java-${VERSION}*/jre/lib/amd64/server/libjvm.so $JAVA_HOME/jre/lib/amd64/server/
  set +x
fi
export PATH=$JAVA_HOME/bin:$PATH
export _JAVA_OPTIONS="-Duser.home=$HOME"

echo "OpenJDK setup complete"
echo ""
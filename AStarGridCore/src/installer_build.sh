jlink \
  --module-path "$JAVA_HOME/jmods" \
  --add-modules java.base,jdk.crypto.ec,java.desktop,java.prefs,jdk.httpserver,java.logging,java.management,java.naming,java.security.jgss,java.instrument \
  --output custom-runtime-linux \
  --compress=2 \
  --strip-debug \
  --no-header-files \
  --no-man-pages



jpackage \
    --type deb \
    --name AStarGrid \
    --app-version 0.0.1 \
    --dest installer-output \
    --input installer \
    --main-jar AStarGridCore-1.0-SNAPSHOT.jar \
    --runtime-image custom-runtime-linux \
    --install-dir /opt/AStarGrid \
    --icon Installer/astargrid.png \
    --verbose
jlink `
  --module-path "$env:JAVA_HOME\jmods" `
  --add-modules java.base,jdk.crypto.ec,java.desktop,java.prefs,jdk.httpserver,java.logging,java.management,java.naming,java.security.jgss,java.instrument   `
  --output custom-runtime `
  --compress=2 `
  --strip-debug `
  --no-header-files `
  --no-man-pages



jpackage `
   --type msi `
   --name AStarGrid `
   --app-version 0.0.1 `
   --dest installer-output `
   --input installer `
   --main-jar AStarGridCore-1.0-SNAPSHOT.jar `
   --runtime-image "custom-runtime" `
   --app-content "installer" `
   --install-dir "AStarGrid" `
   --win-per-user-install `
   --win-console `
   --win-menu `
   --win-menu-group "AStarGrid" `
   --icon "Installer/astargrid.ico" `
   --verbose



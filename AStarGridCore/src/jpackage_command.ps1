jlink `
  --module-path "$env:JAVA_HOME\jmods" `
  --add-modules java.base,java.desktop,java.prefs,jdk.httpserver,java.logging,java.management,java.naming,java.security.jgss,java.instrument   `
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
  --add-modules java.base,java.desktop,java.prefs,jdk.httpserver `
  --win-dir-chooser `
  --win-console `
  --win-menu `
  --win-menu-group "AStarGrid" `
  --icon "Installer/astargrid.ico" `
  --verbose

jpackage `
   --type msi `
   --name AStarGrid `
   --app-version 0.0.1 `
   --dest installer-output `
   --input installer `
   --main-jar AStarGridCore-1.0-SNAPSHOT.jar `
   --runtime-image "AStarGridCore\target\custom-runtime" `
   --app-content "installer" `
   --win-dir-chooser `
   --win-console `
   --win-menu `
   --win-menu-group "AStarGrid" `
   --icon "Installer/astargrid.ico" `
   --verbose

#!/usr/bin/env sh
# This bootstrap requires Gradle 8.13 on PATH for the first invocation.
# CI installs it using gradle/actions/setup-gradle; that command generates an
# official Gradle Wrapper script and JAR before executing project tasks.
if [ -f "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec java -classpath "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
printf '%s\n' 'Gradle wrapper JAR is not bundled. Install Gradle 8.13 and run gradle wrapper --gradle-version 8.13, or use the GitHub Actions workflow.' >&2
exit 1

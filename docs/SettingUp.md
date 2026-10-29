# Setting Up and Getting Started

This page is for developers building MishMash from source. To run the published **v2.0 release**, download its JAR from the [team's Releases page](https://github.com/AY2627S1-CS2113-W13-4/tp/releases) and follow the [User Guide Quick Start](UserGuide.md#quick-start).

## Prerequisites

- Install **JDK 25**. Run `java -version` and `javac -version` in your terminal; both should report version 25.
- Install Git and an IDE with Java support, such as IntelliJ IDEA.
- Allow an internet connection for the Gradle wrapper's first download and dependency resolution.

## Get the Project

1. Fork the [team repository](https://github.com/AY2627S1-CS2113-W13-4/tp) if you will contribute through your own fork.
2. Clone your fork, or clone the team repository for a local working copy:

   ```text
   git clone https://github.com/AY2627S1-CS2113-W13-4/tp.git
   cd tp
   ```

   Cloning downloads the source and Git history. Run the remaining commands from this repository folder.

3. Open the folder as a Gradle project in your IDE. Select **JDK 25** as both the project SDK and the Gradle JVM, then let the Gradle import finish.

## Build and Test

Use the included Gradle wrapper so that everyone uses the project's declared Gradle version.

| Task | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Compile production code | `.\gradlew.bat classes` | `./gradlew classes` |
| Run automated tests | `.\gradlew.bat test` | `./gradlew test` |
| Run all configured build checks | `.\gradlew.bat build` | `./gradlew build` |
| Package the application | `.\gradlew.bat shadowJar` | `./gradlew shadowJar` |
| Start an interactive session | `.\gradlew.bat run` | `./gradlew run` |

The packaged application is written to `build/libs/MishMash.jar`. Start it with:

```text
java -jar build/libs/MishMash.jar
```

Enter your name first, then type `help` to see the commands or `bye` to exit. Application data is held only for the current session.

## Release and Development Versions

The JAR currently published in Releases is **v2.0**. The team has verified that it matches the current source code. The [User Guide](UserGuide.md) labels unimplemented features planned for v2.0b **Coming soon**; these are not available in the v2.0 JAR. Future development checkouts may diverge from this release.

Before publishing another release, check the guides against the exact JAR being distributed. Test that JAR with Java 25; a successful IDE run alone does not verify the release asset.

## Troubleshooting Setup

- **Wrong Java version:** Check the terminal, project SDK, and Gradle JVM separately. Open a new terminal after changing Java settings.
- **Gradle cannot write its cache:** Ensure the configured Gradle user-home folder is writable. For a temporary project-local cache in PowerShell, use `$env:GRADLE_USER_HOME = "$PWD/.gradle/user-home"` before running the wrapper again. This location is already excluded from Git.
- **Dependencies cannot download:** Check your internet connection and any proxy settings, then retry the wrapper.
- **Application closes immediately:** Launch the JAR from a terminal rather than double-clicking it so that prompts and errors remain visible.

Continue with the [Developer Guide](DeveloperGuide.md) for design, testing, and project practices.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Liferay IntelliJ Plugin - an IntelliJ IDEA plugin (Community + Ultimate) that provides Liferay workspace/module creation, server management, deployment actions, and language support for Liferay-specific files (service.xml, bnd.bnd, JSP taglibs, OSGi component properties, etc.).

## Build Commands

```bash
# Build the plugin (also runs source formatting check)
./gradlew clean build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.liferay.ide.idea.language.LiferayXMLSchemaProviderTest"

# Check source formatting (Liferay Source Formatter)
./gradlew checkSourceFormatting

# Fix source formatting
./gradlew formatSource

# Launch a sandboxed IntelliJ instance with the plugin loaded
./gradlew runIde

# Verify plugin compatibility against target IDE versions
./gradlew verifyPlugin

# Build distributable zip (output: build/distributions/)
./gradlew buildPlugin

# Full CI-equivalent check
./gradlew clean test verifyPlugin
```

## Requirements

- Java 21 (Zulu 21.0.6 via sdkman)
- Targets IntelliJ IDEA 2025.2 Ultimate (builds against Ultimate for full feature set)

## Code Style

- Max line length: **120 characters**
- Uses Liferay Source Formatter (`checkSourceFormatting` / `formatSource` tasks)
- `checkSourceFormatting` runs automatically as part of `build`
- Suppressed checks: `JavaStaticImportsCheck`, `MarkdownFileExtensionCheck`

## Architecture

All source lives under `src/main/java/com/liferay/ide/idea/`. The plugin descriptor is at `src/main/resources/META-INF/plugin.xml`.

### Package Layout

| Package | Purpose |
|---------|---------|
| `bnd` | Custom language support for `bnd.bnd` files (lexer, parser, PSI tree, syntax highlighting, completion) |
| `core` | Workspace detection, project type service, startup activities, Gradle resolver extensions |
| `extensions` | Gradle dependency quick fixes |
| `language` | Language support: `blade/`, `gradle/`, `javascript/`, `jsp/`, `osgi/`, `resourcebundle/`, `service/`, `tag/` |
| `server` | Server run configurations, runners, debuggers; `portal/` has bundle abstractions per app server |
| `ui` | Actions (`actions/`), project wizards (`modules/`), watch decorators (`decorator/`), UI components (`compoments/`) |
| `util` | Blade CLI wrappers, Gradle/Maven helpers, file utilities, workspace properties |

### Key Patterns

**WorkspaceProvider SPI** - `LiferayCore.getWorkspaceProvider()` uses `ServiceLoader` to find `LiferayGradleWorkspaceProvider` or `LiferayMavenWorkspaceProvider`. SPI config is in `META-INF/services/`.

**PortalBundle hierarchy** - `AbstractPortalBundle` with concrete implementations for Tomcat, JBoss, WildFly, JBoss EAP. Each handles server-specific startup, JVM args, and configuration.

**Action hierarchy** - `AbstractLiferayAction` -> `AbstractLiferayGradleTaskAction` / `AbstractLiferayMavenGoalAction` -> concrete actions (Deploy, Watch, BuildService, InitBundle).

**Language extensions** follow IntelliJ's extension point model: `CompletionContributor`, `ReferenceContributor`, `LineMarkerProvider`, `LocalInspectionTool`, `ImplicitUsageProvider` registered in `plugin.xml`.

**Conditional extensions** - Feature-specific XML files (CSS, JavaScript, JSP, SASS, web, database, JEE) are loaded only when the corresponding bundled plugin is available. These are declared with `<depends>` in `plugin.xml`.

### Tests

Tests are in `src/test/java/` mirroring the main package structure. They use JUnit 4 with IntelliJ's test framework (`TestFrameworkType.Bundled`). Test fixtures and sample files live in `testdata/`.

### Embedded Resources

- `definitions/` - Downloaded Liferay DTDs, TLDs, and XSDs (7.0-7.4 versions)
- `libs/` - Blade CLI jars (downloaded during build) and `gradle-tooling.jar`
- `configurations/springmvc.json` - Spring MVC portlet template config
- `messages/Messages.properties` - UI strings
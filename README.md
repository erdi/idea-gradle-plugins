[![Build Status](https://snap-ci.com/energizedwork/idea-gradle-plugins/branch/master/build_image)](https://snap-ci.com/energizedwork/idea-gradle-plugins/branch/master)
[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Idea Gradle plugins

This project contains Gradle plugins that allow to control various aspects of IntelliJ configuration generated using Gradle's built-in idea plugin without requiring any knowledge of IntelliJ's config file structure or having to manipulate XML.

## Idea JUnit plugin

This plugin allows to control settings of default JUnit run configuration in IntelliJ.

### Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.energizedwork.idea-junit).

### Usage

#### Applied plugins

This plugin applies Gradle's built-in idea plugin and works by configuring the `idea` extension of that plugin.

#### Extension properties

This plugin exposes the following optional properties through the extension named `ideaJunit`:

| Name | Type | Description |
| --- | --- | --- |
| `systemProperties` | `Map<String, ?>` | Entries are transformed into system properties which are set on the default JUnit run configuration using "VM options" field. |
| `tasks` | `Iterable<String>` | An iterable containing Gradle task names and/or paths in the project. If not empty, execution of the provided tasks will be added to the "Before launch section" of the default JUnit run configuration. |

Example usage:

    ideaJunit {
        tasks = ['pluginUnderTestMetadata']
        systemProperties = ['webdriver.chrome.driver': '/path/to/chromedriver']
    }

#### Tasks

This plugin does not add any tasks to the project.
To use this plugin run the `idea` task from Gradle's built-in idea plugin which is applied by this plugin.

### Building

#### Importing into IDE

The project is setup to generate IntelliJ configuration files.
Simply run `./gradlew idea` and open the generated `*.ipr` file in IntelliJ.

#### Tests

If you import the project into IntelliJ as described above then you can run integration tests even after changing the code without having to perform any manual steps.
They are configured to run in an environment matching the one used when running them using Gradle on the command line.

#### Checking the build

The project contains some code verification tasks aside from tests so if you wish to run a build matching the one on CI then execute `./gradlew check`. 

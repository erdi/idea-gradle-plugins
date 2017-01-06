[![Build Status](https://snap-ci.com/energizedwork/idea-gradle-plugins/branch/master/build_image)](https://snap-ci.com/energizedwork/idea-gradle-plugins/branch/master)
[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Idea Gradle plugins

This project contains Gradle plugins that allow to control various aspects of IntelliJ configuration generated using Gradle's built-in idea plugin without requiring any knowledge of IntelliJ's config file structure or having to manipulate XML.

## Idea Base convention plugin

This convention plugin applies base IntelliJ project configuration, namely to use Git as the VCS and dismiss Gradle project import popup.

### Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.energizedwork.idea-base).

### Usage

#### Applied plugins

This plugin applies [Idea Project Components plugin][#idea-project-components-plugin].

#### Extensions

This plugin does not add any extensions.

#### Tasks

This plugin does not add any tasks to the project.
To use this plugin run the `idea` task from Gradle's built-in idea plugin which is transitively applied by this plugin.

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

## Idea Project Components plugin

This plugin simplifies adding component configurations from different xml sources to IntelliJ project configuration.

### Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.energizedwork.idea-project-components).

### Usage

#### Applied plugins

This plugin applies Gradle's built-in idea plugin and works by configuring the `idea` extension of that plugin.

#### Extension methods

This plugin exposes the following method through the extension named `ideaProjectComponents`:

| Signature | Description |
| --- | --- |
| <code>void&#160;file(Object&#160;file)</code> | Appends contents of an xml file containing a single `component` node as the root element to children of the root element of the `ipr` file. If a `component` node with the same name already exists in the ipr file then it is replaced. Anything that can be passed to [Project#file(Object)](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#file(java.lang.Object)) can be used as an argument. |
| <code>void&#160;stream(InputStream&#160;stream)</code> | Appends contents of an xml stream containing a single `component` node as the root element to children of the root element of the `ipr` file. If a `component` node with the same name already exists in the ipr file then it is replaced. |

Example usage:

    ideaProjectComponents {
        file 'code-style.xml'
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

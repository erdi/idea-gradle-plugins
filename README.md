[![Build Status](https://circleci.com/gh/erdi/idea-gradle-plugins/tree/master.svg?style=shield&circle-token=652fc829e0e8890b9165135133ce077ce6b5ba38)](https://circleci.com/gh/erdi/idea-gradle-plugins/tree/master)
[![License](https://img.shields.io/badge/license-ASL2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Idea Gradle plugins

This project contains a Gradle plugin that allows to control various aspects of IntelliJ configuration generated using Gradle's built-in idea plugin without requiring any knowledge of IntelliJ's config file structure or having to manipulate XML as well as a convention plugin which applies base IntelliJ project configuration.

## Idea Base convention plugin

This convention plugin applies base IntelliJ project configuration, namely to use Git as the VCS and dismiss Gradle project import popup.
It also bootstraps a default remote debug run configuration and disables code and todo analysis when committing code.

### Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.github.erdi.idea-base).

### Usage

This plugin is designed to be applied onto the root project and has no effect if applied to a subproject.

#### Applied plugins

This plugin applies [Idea configuration extensions plugin](#idea-configuration-extensions-plugin).

#### Extensions

This plugin does not add any extensions.

#### Tasks

This plugin does not add any tasks to the project.
To use this plugin run the `idea` task from Gradle's built-in idea plugin which is transitively applied by this plugin.

## Idea configuration extensions plugin

This plugin allows to control various aspects of IntelliJ configuration generated using Gradle's built-in idea plugin without requiring any knowledge of IntelliJ's config file structure or having to manipulate XML.

### Installation

For installation instructions please see [this plugin's page on Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.github.erdi.extended-idea).

### Usage

This plugin is designed to be applied onto the root project and has no effect if applied to a subproject.

#### Applied plugins

This plugin applies Gradle's built-in idea plugin and works by configuring the `idea` extension of that plugin.

#### Extension properties

This plugin adds an extension called `extended` to the `idea` extension of Gradle's built-in idea plugin. 
There are several nested configuration properties hanging off the `extended` extension and they are described below.

##### Modifying default JUnit run configuration 

It's possible to modify the default JUnit run configuration via the following properties of `idea.extended.workspace.junit`:

| Name | Type | Description |
| --- | --- | --- |
| `systemProperties` | `Map<String, ?>` | Entries are transformed into system properties which are set on the default JUnit run configuration using "VM options" field. |
| `tasks` | `Iterable<String>` | An iterable containing Gradle task names and/or paths in the project. If not empty, execution of the provided tasks will be added to the "Before launch section" of the default JUnit run configuration. |

Example usage:

    idea {
        extended {
            workspace {
                junit {
                    tasks = ['pluginUnderTestMetadata']
                    systemProperties = ['webdriver.chrome.driver': '/path/to/chromedriver']
                }
            }
        }
    }

##### Adding project component configurations

IntelliJ project component configurations can be added via the following methods of `idea.extended.project.components`:

| Signature | Description |
| --- | --- |
| <code>void&#160;file(Object&#160;file)</code> | Appends contents of an xml file containing a single `component` node as the root element to children of the root element of the `ipr` file. If a `component` node with the same name already exists in the ipr file then it is replaced. Anything that can be passed to [Project#file(Object)](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#file(java.lang.Object)) can be used as an argument. |
| <code>void&#160;stream(InputStream&#160;stream)</code> | Appends contents of an xml stream containing a single `component` node as the root element to children of the root element of the `ipr` file. If a `component` node with the same name already exists in the ipr file then it is replaced. |

Example usage:

    idea {
        extended {
            project {
                components {
                    file 'code-style.xml'
                }
            }
        }
    }

##### Adding workspace component configurations

IntelliJ workspace component configurations can be added via the following methods of `idea.extended.workspace.components`:

| Signature | Description |
| --- | --- |
| <code>void&#160;file(Object&#160;file)</code> | Appends contents of an xml file containing a single `component` node as the root element to children of the root element of the `iws` file. If a `component` node with the same name already exists in the iws file then it is replaced. Anything that can be passed to [Project#file(Object)](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#file(java.lang.Object)) can be used as an argument. |
| <code>void&#160;stream(InputStream&#160;stream)</code> | Appends contents of an xml stream containing a single `component` node as the root element to children of the root element of the `iws` file. If a `component` node with the same name already exists in the ipr file then it is replaced. |

Example usage:

    idea {
        extended {
            workspace {
                components {
                    file 'vcs-manager-configuration.xml'
                }
            }
        }
    }

##### Setting workspace properties

Intellij workspace properties as specified in the `PropertiesComponent` component can be controlled via `properties()` method of `idea.extended.workspace`.
The method takes a single argument, a `Map<String, String>`.

Example usage:

    idea {
        extended {
            workspace {
                properties('show.inlinked.gradle.project.popup': 'false')
            }
        }
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

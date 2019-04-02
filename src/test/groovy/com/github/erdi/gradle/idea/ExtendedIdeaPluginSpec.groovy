/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.erdi.gradle.idea

class ExtendedIdeaPluginSpec extends PluginSpec {

    final String pluginId = 'com.github.erdi.extended-idea'

    def "configuring tasks to be executed before running tests in IntelliJ"() {
        given:
        configurePlugin """
            workspace {
                junit {
                    tasks = [${taskNames.collect { "'$it'" }.join(', ')}]
                }
            }
        """

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        with(defaultJunitConf.method.option[0]) {
            attribute('name') == 'Gradle.BeforeRunTask'
            attribute('enabled') == 'true'
            attribute('tasks') == taskNames.join(' ')
            attribute('externalProjectPath') == owner.buildScript.canonicalPath
        }
        with(defaultJunitConf.method.option[1]) {
            attribute('name') == 'Make'
            attribute('enabled') == 'true'
        }

        where:
        taskNames = ['foo', 'bar']
    }

    def "configuring system properties to be set when running tests in IntelliJ"() {
        given:
        configurePlugin """
            workspace {
                junit {
                    systemProperties = [${testProperties.collect { "${it.key}: '${it.value}'" }.join(', ')}]
                }
            }
        """

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        defaultJunitConf.option.find { it.@name == 'VM_PARAMETERS' }.@value ==
                testProperties.collect { "-D${it.key}=${it.value}" }.join(' ')

        where:
        testProperties = [foo: 'bar', fizz: 'buzz']
    }

    def "configuring environment variables to be set when running tests in IntelliJ"() {
        given:
        configurePlugin """
            workspace {
                junit {
                    environment = [${testProperties.collect { "${it.key}: '${it.value}'" }.join(', ')}]
                }
            }
        """

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        defaultJunitConf.envs.env.collectEntries { [(it.@name): it.@value] } == testProperties

        where:
        testProperties = [foo: 'bar', fizz: 'buzz']
    }

    def "when no tasks are configured to be executed before running tests then config is not modified"() {
        given:
        applyPlugin()

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        defaultJunitConf.method.option*.attribute('name') != ['Gradle.BeforeRunTask', 'Make']
    }

    def "when no system properties are configured to be executed before running tests then config is not modified"() {
        given:
        configurePlugin '''
            workspace {
                junit {
                    systemProperties = null
                }
            }
        '''

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        !defaultJunitConf.option.find { it.@name == 'VM_PARAMETERS' }.@value
    }

    def "when no environment variables are configured to be executed before running tests then config is not modified"() {
        given:
        configurePlugin '''
            workspace {
                junit {
                    environment = null
                }
            }
        '''

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        !defaultJunitConf.envs.env
    }

    def "applying plugin to subprojects does not cause errors"() {
        given:
        def subprojectBuildScript = new File(testProjectDir.newFolder(subprojectName), 'build.gradle')

        and:
        applyPlugin(subprojectBuildScript)
        settingsFile << """
            include ':$subprojectName'
        """

        when:
        runTask('idea')

        then:
        noExceptionThrown()

        where:
        subprojectName = 'subproject'
    }

    def "adding project component configuration from files"() {
        given:
        testProjectDir.newFile(componentFileName) << """
            <component name="$componentName"></component>
        """

        configurePlugin """
            project {
                components {
                    file '$componentFileName'
                }
            }
        """

        when:
        def iprXml = generateAndParseIdeaProjectConf()

        then:
        iprXml.component.find { it.@name == componentName }

        where:
        componentFileName = 'testComponent.xml'
        componentName = 'TestComponent'
    }

    def "adding project component configuration from stream"() {
        given:
        def componentFile = testProjectDir.newFile(componentFileName) << """
            <component name="$componentName"></component>
        """

        configurePlugin """
            project {
                components {
                    stream new File("$componentFile.canonicalPath").newDataInputStream()
                }
            }
        """

        when:
        def iprXml = generateAndParseIdeaProjectConf()

        then:
        iprXml.component.find { it.@name == componentName }

        where:
        componentFileName = 'testComponent.xml'
        componentName = 'TestComponent'
    }

    def "adding project component configuration overrides any existing configuration for that component"() {
        given:
        testProjectDir.newFile(componentFileName) << """
            <component name="$existingComponentName">
                <option name="$optionName"></option>
            </component>
        """

        configurePlugin """
            project {
                components {
                    file '$componentFileName'
                }
            }
        """

        when:
        def existingComponentConfiguration = generateAndParseIdeaProjectConf().component.findAll { it.@name == existingComponentName }

        then:
        existingComponentConfiguration.size() == 1
        existingComponentConfiguration.first().option*.@name == [optionName]

        where:
        componentFileName = 'copyrightManager.xml'
        existingComponentName = 'CopyrightManager'
        optionName = 'TestOption'
    }

    def "applying plugin to subprojects and adding project component configuration does not cause errors"() {
        given:
        def subprojectDir = testProjectDir.newFolder(subprojectName)
        def subprojectBuildScript = new File(subprojectDir, 'build.gradle')
        settingsFile << """
            include ':$subprojectName'
        """

        and:
        new File(subprojectDir, componentFileName) << '''
            <component/>
        '''

        configurePlugin("""
            project {
                components {
                    file '$componentFileName'
                }
            }
        """, subprojectBuildScript)

        when:
        runTask('idea')

        then:
        noExceptionThrown()

        where:
        subprojectName = 'subproject'
        componentFileName = 'testComponent.xml'
    }

    def "adding workspace component configuration from files"() {
        given:
        testProjectDir.newFile(componentFileName) << """
            <component name="$componentName"></component>
        """

        configurePlugin """
            workspace {
                components {
                    file '$componentFileName'
                }
            }
        """

        when:
        def iwsXml = generateAndParseIdeaWorkspaceConf()

        then:
        iwsXml.component.find { it.@name == componentName }

        where:
        componentFileName = 'testComponent.xml'
        componentName = 'TestComponent'
    }

    def "adding workspace component configuration from stream"() {
        given:
        def componentFile = testProjectDir.newFile(componentFileName) << """
            <component name="$componentName"></component>
        """

        configurePlugin """
            workspace {
                components {
                    stream new File("$componentFile.canonicalPath").newDataInputStream()
                }
            }
        """

        when:
        def iwsXml = generateAndParseIdeaWorkspaceConf()

        then:
        iwsXml.component.find { it.@name == componentName }

        where:
        componentFileName = 'testComponent.xml'
        componentName = 'TestComponent'
    }

    def "adding workspace component configuration overrides any existing configuration for that component"() {
        given:
        testProjectDir.newFile(componentFileName) << """
            <component name="$existingComponentName">
                <option name="$optionName"></option>
            </component>
        """

        configurePlugin """
            workspace {
                components {
                    file '$componentFileName'
                }
            }
        """

        when:
        def existingComponentConfiguration = generateAndParseIdeaWorkspaceConf().component.findAll { it.@name == existingComponentName }

        then:
        existingComponentConfiguration.size() == 1
        existingComponentConfiguration.first().option*.@name == [optionName]

        where:
        componentFileName = 'changeListManager.xml'
        existingComponentName = 'ChangeListManager'
        optionName = 'TestOption'
    }

    def "applying plugin to subprojects and adding workspace component configuration does not cause errors"() {
        given:
        def subprojectDir = testProjectDir.newFolder(subprojectName)
        def subprojectBuildScript = new File(subprojectDir, 'build.gradle')
        settingsFile << """
            include ':$subprojectName'
        """

        and:
        new File(subprojectDir, componentFileName) << '''
            <component/>
        '''

        configurePlugin("""
            workspace {
                components {
                    file '$componentFileName'
                }
            }
        """, subprojectBuildScript)

        when:
        runTask('idea')

        then:
        noExceptionThrown()

        where:
        subprojectName = 'subproject'
        componentFileName = 'testComponent.xml'
    }

    def "updating an existing workspace property"() {
        given:
        configurePlugin """
            workspace {
                properties('$name': '$value')
            }
        """

        when:
        def iws = generateAndParseIdeaWorkspaceConf()

        then:
        propertyValue(iws, name) == value

        where:
        name = 'GoToFile.includeJavaFiles'
        value = 'true'
    }

    def "adding a new workspace property"() {
        given:
        configurePlugin """
            workspace {
                properties('$name': '$value')
            }
        """

        when:
        def iws = generateAndParseIdeaWorkspaceConf()

        then:
        propertyValue(iws, name) == value

        where:
        name = 'show.inlinked.gradle.project.popup'
        value = 'false'
    }

    def "applying plugin to subprojects and configuring workspace properties does not cause errors"() {
        given:
        def subprojectDir = testProjectDir.newFolder(subprojectName)
        def subprojectBuildScript = new File(subprojectDir, 'build.gradle')
        settingsFile << """
            include ':$subprojectName'
        """

        and:
        configurePlugin("""
            workspace {
                properties(key: 'value')
            }
        """, subprojectBuildScript)

        when:
        runTask('idea')

        then:
        noExceptionThrown()

        where:
        subprojectName = 'subproject'
    }

    private void configurePlugin(String configuration = '', File buildScript = buildScript) {
        applyPlugin(buildScript)
        buildScript << """
            idea {
                extended {
                    $configuration
                }
            }
        """
    }

    private Node generateAndParseJunitConf() {
        def runManager = generateAndParseRunManagerConf()
        runManager.configuration.find { it.@default == 'true' && it.'@type' == 'JUnit' }
    }

    private void runIdeaProjectTask() {
        runTask('ideaProject')
    }

    private Node generateAndParseIdeaProjectConf() {
        runIdeaProjectTask()
        new XmlParser().parse(new File(testProjectDir.root, "${TEST_PROJECT_NAME}.ipr"))
    }
}

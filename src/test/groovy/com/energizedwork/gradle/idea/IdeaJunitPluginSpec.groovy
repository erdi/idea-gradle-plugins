/*
 * Copyright 2016 the original author or authors.
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
package com.energizedwork.gradle.idea

class IdeaJunitPluginSpec extends ConfigurablePluginSpec {

    final String pluginId = 'com.energizedwork.idea-junit'
    final String pluginName = IdeaJunitPlugin.NAME

    def "configuring tasks to be executed before running tests in IntelliJ"() {
        given:
        configurePlugin """
            tasks = [${taskNames.collect { "'$it'" }.join(', ')}]
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
            systemProperties = [${testProperties.collect { "${it.key}: '${it.value}'" }.join(', ')}]
        """

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        defaultJunitConf.option.find { it.@name == 'VM_PARAMETERS' }.@value ==
                testProperties.collect { "-D${it.key}=${it.value}" }.join(' ')

        where:
        testProperties = [foo: 'bar', fizz: 'buzz']
    }

    def "when no tasks are configured to be executed before running tests then config is not modified"() {
        given:
        configurePlugin()

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        defaultJunitConf.method.option*.attribute('name') != ['Gradle.BeforeRunTask', 'Make']
    }

    def "when no system properties are configured to be executed before running tests then config is not modified"() {
        given:
        configurePlugin '''
            systemProperties = null
        '''

        when:
        def defaultJunitConf = generateAndParseJunitConf()

        then:
        !defaultJunitConf.option.find { it.@name == 'VM_PARAMETERS' }.@value
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

    private Node generateAndParseJunitConf() {
        def runManager = generateAndParseRunManagerConf()
        runManager.configuration.find { it.@default == 'true' && it.'@type' == 'JUnit' }
    }

}

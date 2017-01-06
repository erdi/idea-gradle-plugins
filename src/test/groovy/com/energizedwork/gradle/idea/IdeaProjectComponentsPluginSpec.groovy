/*
 * Copyright 2017 the original author or authors.
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

class IdeaProjectComponentsPluginSpec extends ConfigurablePluginSpec {

    final String pluginName = IdeaProjectComponentsPlugin.NAME
    final String pluginId = 'com.energizedwork.idea-project-components'

    def "adding project component configuration from files"() {
        given:
        testProjectDir.newFile(componentFileName) << """
            <component name="$componentName"></component>
        """

        configurePlugin """
            file '$componentFileName'
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
            stream new File("$componentFile.canonicalPath").newDataInputStream()
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
            file '$componentFileName'
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

    private void runIdeaProjectTask() {
        runTask('ideaProject')
    }

    private Node generateAndParseIdeaProjectConf() {
        runIdeaProjectTask()
        new XmlParser().parse(new File(testProjectDir.root, "${TEST_PROJECT_NAME}.ipr"))
    }

}

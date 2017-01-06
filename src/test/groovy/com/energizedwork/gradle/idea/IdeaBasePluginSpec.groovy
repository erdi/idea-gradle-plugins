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

import groovy.xml.XmlUtil
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input

import static org.xmlunit.builder.Input.fromStream

class IdeaBasePluginSpec extends PluginSpec {

    final String pluginId = 'com.energizedwork.idea-base'

    def "applying plugin sets up vcs and gradle project import settings"() {
        given:
        applyPlugin()

        when:
        def iprXml = generateAndParseIdeaProjectConf()

        then:
        def vcsMapping = iprXml.component.find { it.@name == 'VcsDirectoryMappings' }.mapping.first()
        vcsMapping.@directory == ''
        vcsMapping.@vcs == 'Git'

        and:
        !DiffBuilder.compare(gradleSettingsResourceInput)
                .withTest(nodeInput(iprXml.component.find { it.@name == 'GradleSettings' }))
                .ignoreWhitespace()
                .build()
                .hasDifferences()
    }

    private Input.Builder nodeInput(Node node) {
        Input.fromString(XmlUtil.serialize(node))
    }

    private Input.Builder getGradleSettingsResourceInput() {
        fromStream(getClass().getResourceAsStream('gradle-settings.xml'))
    }

    private void runIdeaProjectTask() {
        runTask('ideaProject')
    }

    private Node generateAndParseIdeaProjectConf() {
        runIdeaProjectTask()
        new XmlParser().parse(new File(testProjectDir.root, "${TEST_PROJECT_NAME}.ipr"))
    }

}

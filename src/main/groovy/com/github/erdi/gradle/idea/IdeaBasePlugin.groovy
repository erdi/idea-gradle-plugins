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
package com.github.erdi.gradle.idea

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.plugins.ide.idea.model.IdeaModel

class IdeaBasePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.pluginManager.apply(ExtendedIdeaPlugin)

        def extensions = project.extensions
        setupVcs(extensions)
        setupGradleImportSettings(extensions)
        setupDebugRunConfiguration(project)
        setupVcsManagerSettings(extensions)
    }

    private void setupVcs(ExtensionContainer extensions) {
        extensions.getByType(IdeaModel).project?.vcs = 'Git'
    }

    private void setupGradleImportSettings(ExtensionContainer extensions) {
        extendedIdea(extensions).workspace.properties(
                'show.inlinked.gradle.project.popup': false.toString(),
                'show.unlinked.gradle.project.popup': false.toString()
        )
    }

    private void setupDebugRunConfiguration(Project project) {
        project.extensions.configure(IdeaModel) {
            it.workspace?.iws?.withXml { provider ->
                def addedConfiguration = new XmlParser().parse(getClass().getResourceAsStream('debug-run-configuration.xml'))

                def runManager = provider.asNode().component.find { it.'@name' == 'RunManager' }
                def replacedConfiguration = runManager.configuration.find { it.@name == 'Debug' }

                replacedConfiguration ? replacedConfiguration.replaceNode(addedConfiguration) : runManager.append(addedConfiguration)
            }
        }
    }

    private void setupVcsManagerSettings(ExtensionContainer extensions) {
        def vcsManagerXmlStream = getClass().getResourceAsStream('vcs-manager-configuration.xml')
        extendedIdea(extensions).workspace.components.stream(vcsManagerXmlStream)
    }

    private ExtendedIdeaPluginExtension extendedIdea(ExtensionContainer extensions) {
        def ideaModel = extensions.getByType(IdeaModel) as ExtensionAware
        ideaModel.extensions.getByType(ExtendedIdeaPluginExtension)
    }

}

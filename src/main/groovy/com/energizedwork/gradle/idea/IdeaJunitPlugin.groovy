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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

class IdeaJunitPlugin implements Plugin<Project> {

    private static final String SPACE = ' '
    public static final String NAME = 'ideaJunit'

    void apply(Project project) {
        project.pluginManager.apply(IdeaPlugin)
        IdeaJunitPluginExtension extension = project.extensions.create(NAME, IdeaJunitPluginExtension)
        setupDefaultJunitConfiguration(project, extension)
    }

    void setupDefaultJunitConfiguration(Project project, IdeaJunitPluginExtension ideaJunitPluginExtension) {
        project.extensions.configure(IdeaModel) {
            it.workspace?.iws?.withXml { provider ->
                def tasksAttribute = ideaJunitPluginExtension.tasks.join(SPACE)
                def node = provider.asNode()
                def runManager = node.component.find { it.'@name' == 'RunManager' }

                def defaultJUnitConf = runManager.configuration.find {
                    it.'@default' == 'true' && it.'@type' == 'JUnit'
                }

                if (ideaJunitPluginExtension.tasks) {
                    defaultJUnitConf.method.replaceNode {
                        method {
                            option(
                                    name: 'Gradle.BeforeRunTask',
                                    enabled: true,
                                    tasks: tasksAttribute,
                                    externalProjectPath: project.buildFile.canonicalPath
                            )
                            option(name: 'Make', enabled: true)
                        }
                    }
                }

                def vmParamsNode = defaultJUnitConf.option.find { it.@name == 'VM_PARAMETERS' }
                def vmParams = ideaJunitPluginExtension.systemProperties
                        .collect { "-D${it.key}=${it.value}" }.join(SPACE)
                vmParamsNode.@value = vmParams
            }
        }
    }
}

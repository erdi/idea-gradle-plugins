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
package com.energizedwork.gradle.idea

import org.gradle.api.Project
import org.gradle.plugins.ide.idea.model.IdeaModel

import static org.gradle.util.ConfigureUtil.configure

class ExtendedIdeaWorkspace {

    private static final String SPACE = ' '

    private final Project project

    final junit = new IdeaJunit()

    ExtendedIdeaWorkspace(Project project) {
        this.project = project
        setupDefaultJunitConfiguration(project, junit)
    }

    @SuppressWarnings('ConfusingMethodName')
    void junit(@DelegatesTo(IdeaJunit) Closure<?> configuration) {
        configure(configuration, junit)
    }

    void setupDefaultJunitConfiguration(Project project, IdeaJunit junit) {
        project.extensions.configure(IdeaModel) {
            it.workspace?.iws?.withXml { provider ->
                def tasksAttribute = junit.tasks.join(SPACE)
                def node = provider.asNode()
                def runManager = node.component.find { it.'@name' == 'RunManager' }

                def defaultJUnitConf = runManager.configuration.find {
                    it.'@default' == 'true' && it.'@type' == 'JUnit'
                }

                if (junit.tasks) {
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
                def vmParams = junit.systemProperties
                        .collect { "-D${it.key}=${it.value}" }.join(SPACE)
                vmParamsNode.@value = vmParams
            }
        }
    }

}

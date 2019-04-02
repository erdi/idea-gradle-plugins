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

import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.plugins.ide.idea.model.IdeaModel

import static org.gradle.util.ConfigureUtil.configure

class ExtendedIdeaWorkspace {

    private static final String SPACE = ' '

    private final Project project
    protected final Map<String, String> propertyValues = [:]

    final IdeaJunit junit = new IdeaJunit()

    final IdeaComponents components

    ExtendedIdeaWorkspace(Project project) {
        this.project = project
        this.components = new IdeaComponents(project, project.extensions.getByType(IdeaModel).workspace?.iws)
        setupDefaultJunitConfiguration()
        updatePropertiesComponent()
    }

    @SuppressWarnings('ConfusingMethodName')
    void junit(@DelegatesTo(IdeaJunit) Closure<?> configuration) {
        configure(configuration, junit)
    }

    @SuppressWarnings('ConfusingMethodName')
    void components(@DelegatesTo(IdeaComponents) Closure<?> configuration) {
        configure(configuration, components)
    }

    void properties(Map<String, String> properties) {
        propertyValues << properties
    }

    private void setupDefaultJunitConfiguration() {
        withXml { XmlProvider provider ->
            def node = provider.asNode()
            def runManager = node.component.find { it.'@name' == 'RunManager' }
            def defaultJUnitConf = runManager.configuration.find {
                it.'@default' == 'true' && it.'@type' == 'JUnit'
            }

            setupDefaultJunitGradleTasks(defaultJUnitConf)
            setupDefaultJunitSystemProperties(defaultJUnitConf)
            setupDefaultJunitEnvironmentVariables(defaultJUnitConf)
        }
    }

    protected void setupDefaultJunitGradleTasks(Node defaultJUnitConf) {
        if (junit.tasks) {
            def tasksAttribute = junit.tasks.join(SPACE)

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
    }

    protected void setupDefaultJunitSystemProperties(Node defaultJUnitConf) {
        def vmParamsNode = defaultJUnitConf.option.find { it.@name == 'VM_PARAMETERS' }
        def vmParams = junit.systemProperties.collect { "-D${it.key}=${it.value}" }.join(SPACE)
        vmParamsNode.@value = vmParams
    }

    protected void setupDefaultJunitEnvironmentVariables(Node defaultJUnitConf) {
        if (junit.environment) {
            def envs = defaultJUnitConf.envs.first()
            junit.environment.each { name, value ->
                findOrAddEnv(envs, name).@value = value.toString()
            }
        }
    }

    private void updatePropertiesComponent() {
        withXml { XmlProvider provider ->
            def node = provider.asNode()
            def propertiesComponent = node.component.find { it.'@name' == 'PropertiesComponent' } as Node

            propertyValues.each { key, value ->
                findOrAddProperty(propertiesComponent, key).@value = value
            }
        }
    }

    private void withXml(Closure<?> configuration) {
        project.extensions.configure(IdeaModel) {
            it.workspace?.iws?.withXml(configuration)
        }
    }

    protected Node findOrAddProperty(Node propertiesComponent, String name) {
        propertiesComponent.property.find { it.'@name' == name } as Node ?: propertiesComponent.appendNode('property', [name: name])
    }

    private Node findOrAddEnv(Node envs, String name) {
        envs.env.find { it.'@name' == name } as Node ?: envs.appendNode('env', [name: name])
    }

}

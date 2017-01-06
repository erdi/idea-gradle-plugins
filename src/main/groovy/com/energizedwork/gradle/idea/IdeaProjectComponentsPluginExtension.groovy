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

import org.gradle.api.Project
import org.gradle.plugins.ide.idea.model.IdeaModel

class IdeaProjectComponentsPluginExtension {

    protected final Project project

    IdeaProjectComponentsPluginExtension(Project project) {
        this.project = project
    }

    void file(Object file) {
        project.extensions.getByType(IdeaModel).project.ipr.withXml { provider ->
            def addedComponent = new XmlParser().parse(project.file(file))
            def node = provider.asNode()

            def replacedComponent = node.component.find { it.@name == addedComponent.@name }

            replacedComponent ? replacedComponent.replaceNode(addedComponent) : node.append(addedComponent)
        }
    }

}

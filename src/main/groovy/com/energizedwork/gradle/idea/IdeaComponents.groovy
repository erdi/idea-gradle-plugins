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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Project
import org.gradle.plugins.ide.api.XmlFileContentMerger

class IdeaComponents {

    private final Project project
    private final XmlFileContentMerger xmlMerger

    IdeaComponents(Project project, XmlFileContentMerger xmlMerger) {
        this.project = project
        this.xmlMerger = xmlMerger
    }

    void file(Object file) {
        add { it.parse(project.file(file)) }
    }

    void stream(InputStream stream) {
        add { it.parse(stream) }
    }

    private void add(@ClosureParams(value = SimpleType, options = 'groovy.util.XmlParser') Closure<Node> nodeProvider) {
        xmlMerger?.withXml { provider ->
            def addedComponent = nodeProvider.call(new XmlParser())
            def node = provider.asNode()

            def replacedComponent = node.component.find { it.@name == addedComponent.@name }

            replacedComponent ? replacedComponent.replaceNode(addedComponent) : node.append(addedComponent)
        }
    }

}

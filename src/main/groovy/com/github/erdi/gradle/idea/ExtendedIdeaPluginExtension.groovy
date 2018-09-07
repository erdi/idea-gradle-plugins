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

import static org.gradle.util.ConfigureUtil.configure

class ExtendedIdeaPluginExtension {

    final ExtendedIdeaProject project
    final ExtendedIdeaWorkspace workspace

    ExtendedIdeaPluginExtension(Project project) {
        this.project = new ExtendedIdeaProject(project)
        this.workspace = new ExtendedIdeaWorkspace(project)
    }

    @SuppressWarnings('ConfusingMethodName')
    void project(@DelegatesTo(ExtendedIdeaProject) Closure<?> configuration) {
        configure(configuration, project)
    }

    @SuppressWarnings('ConfusingMethodName')
    void workspace(@DelegatesTo(ExtendedIdeaWorkspace) Closure<?> configuration) {
        configure(configuration, workspace)
    }

}

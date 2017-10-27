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
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.plugins.ide.idea.model.IdeaModel

class IdeaBasePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.pluginManager.apply(IdeaProjectComponentsPlugin)

        def extensions = project.extensions
        setupVcs(extensions)
        setupGradleImportSettings(extensions)
    }

    private void setupVcs(ExtensionContainer extensions) {
        extensions.getByType(IdeaModel).project?.vcs = 'Git'
    }

    private void setupGradleImportSettings(ExtensionContainer extensions) {
        def gradleSettingsXmlStream = getClass().getResourceAsStream('gradle-settings.xml')
        extensions.getByType(IdeaProjectComponentsPluginExtension).stream(gradleSettingsXmlStream)
    }

}

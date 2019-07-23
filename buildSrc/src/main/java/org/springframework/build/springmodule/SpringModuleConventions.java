/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.build.springmodule;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.PluginManager;

import org.springframework.build.compile.CompilePlugin;
import org.springframework.build.javadoc.JavadocPlugin;
import org.springframework.build.optional.OptionalDependenciesPlugin;
import org.springframework.build.publish.MavenPublishConventions;
import org.springframework.build.testsources.TestSourcesPlugin;

/**
 * A {@link Plugin} that applies all relevant Spring Framework plugins
 * and conventions for building a Spring Framework module:
 * <ul>
 *     <li>{@link OptionalDependenciesPlugin} for adding the {@code optional} configuration
 *     <li>{@link CompilePlugin} for configuring common compile options and jar packaging conventions
 *     <li>{@link JavadocPlugin} for generating Javadoc and Dokka and packaging them in {@code -javadoc.jar} archives
 *     <li>{@link TestSourcesPlugin} for adding test sources of other projects to the current project's classpath
 *     <li>{@link MavenPublishConventions} for publishing module artifacts (including source and javadoc jars)
 * </ul>
 *
 * @author Brian Clozel
 */
public class SpringModuleConventions implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(JavaLibraryPlugin.class);
		pluginManager.apply(OptionalDependenciesPlugin.class);
		pluginManager.apply(CompilePlugin.class);
		pluginManager.apply(JavadocPlugin.class);
		pluginManager.apply(TestSourcesPlugin.class);
		pluginManager.apply(MavenPublishConventions.class);
	}
}

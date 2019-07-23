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

package org.springframework.build.checks;

import java.net.URI;

import io.spring.nohttp.gradle.NoHttpExtension;
import io.spring.nohttp.gradle.NoHttpPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;

/**
 * {@link Plugin} that applies and configures checks on project sources,
 * including {@link NoHttpPlugin} and {@link CheckstylePlugin}.
 * 
 * @author Brian Clozel
 */
public class CheckSourcesPlugin implements Plugin<Project> {

	private final static String SPRING_JAVAFORMAT_CHECKSTYLE_DEPENDENCY =
			"io.spring.javaformat:spring-javaformat-checkstyle:0.0.7";

	@Override
	public void apply(Project project) {
		applyCheckStyle(project.getRootProject());
		applyNoHttpPlugin(project.getRootProject());
	}

	private void applyCheckStyle(Project project) {
		project.allprojects(p -> {
			p.getPluginManager().apply(CheckstylePlugin.class);
			p.getDependencies().add("checkstyle", SPRING_JAVAFORMAT_CHECKSTYLE_DEPENDENCY);
			p.getExtensions().configure(CheckstyleExtension.class, checkstyle -> {
				checkstyle.setToolVersion("8.23");
				checkstyle.setConfigDir(p.getRootProject().file("src/checkstyle"));
			});
		});
	}

	private void applyNoHttpPlugin(Project project) {
		project.getPluginManager().apply(NoHttpPlugin.class);
		project.getExtensions().configure(NoHttpExtension.class, nohttp -> {
			ConfigurableFileTree source = (ConfigurableFileTree) nohttp.getSource();
			source.exclude("**/test-output/**");
			nohttp.setWhitelistFile(project.file("src/nohttp/whitelist.lines"));
			URI projectDir = project.getProjectDir().toURI();
			project.allprojects(p -> {
				URI outURI = p.file("out").toURI();
				String pattern = projectDir.relativize(outURI).getPath() + "**";
				source.exclude(pattern);
			});
		});
	}

}

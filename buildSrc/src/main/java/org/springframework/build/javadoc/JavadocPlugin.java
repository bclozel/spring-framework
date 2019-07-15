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

package org.springframework.build.javadoc;

import java.io.File;
import java.util.Collections;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.dokka.gradle.DokkaPlugin;
import org.jetbrains.dokka.gradle.DokkaTask;

/**
 * {@link Plugin} that creates Javadoc tasks for creating {@code "-javadoc.jar"} artifacts
 * on each module and a main Javadoc task for publishing API docs on the official website.
 * It also configures the main Dokka task for publishing kotlin API docs.
 *
 * @author Brian Clozel
 */
public class JavadocPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		if (project.getRootProject() == project) {
			project.getPlugins()
					.withType(JavadocPlugin.class, plugin -> createJavadocApiTask(project));
			project.getPluginManager().apply(DokkaPlugin.class);
			project.getPlugins()
					.withType(DokkaPlugin.class, plugin -> configureDokkaApiTask(project));
		}
		else {
			project.getPlugins().withType(JavadocPlugin.class, plugin -> configureJavadocModuleTask(project));
		}
		project.getPlugins().withType(JavadocPlugin.class, plugin ->
				project.getPluginManager().apply(JavadocConventions.class));
	}

	/**
	 * Create a task for generating javadoc for a given project module,
	 * to be published as a {@code "-javadoc.jar"} artifact.
	 */
	private void configureJavadocModuleTask(Project project) {
		project.getTasks().withType(Javadoc.class).forEach(javadoc -> {
			javadoc.setDescription("Generates project-level javadoc for use in -javadoc jar");
			// Suppress warnings due to cross-module @see and @link references.
			// Note that global 'api' task does display all warnings.
			javadoc.getLogging().captureStandardError(LogLevel.INFO);
			javadoc.getLogging().captureStandardOutput(LogLevel.INFO);
		});
	}

	/**
	 * Create a task for generating javadoc for the whole Spring Framework,
	 * to be published as HTML files on the official website.
	 */
	private void createJavadocApiTask(Project project) {
		Javadoc javadoc = project.getTasks().create("api", Javadoc.class);
		javadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
		javadoc.setDescription("Generates aggregated Javadoc API documentation.");
		javadoc.setDestinationDir(new File(project.getBuildDir(), "docs/api"));
		javadoc.setMaxMemory("1024m");

		javadoc.setSource(project.getSubprojects().stream()
				.map(this::getCompileClasspath)
				.collect(Collectors.toList()));

		javadoc.doFirst(t -> {
			// ensure the javadoc process can resolve types compiled from .aj sources
			final JavaPluginConvention springAspects = project
					.project(":spring-aspects").getConvention()
					.getPlugin(JavaPluginConvention.class);
			SourceSetOutput aspectsOutput = springAspects.getSourceSets().findByName("main").getOutput();
			FileCollection subProjectsClasspath = project.getSubprojects().stream()
					.map(p -> getCompileClasspath(p)).reduce((first, second) -> first.plus(second)).get();
			javadoc.setClasspath(aspectsOutput.plus(subProjectsClasspath));
		});
		javadoc.setDependsOn(project.getSubprojects().stream()
				.map(p -> p.getTasksByName("jar", false))
				.collect(Collectors.toList()));
	}

	private FileCollection getCompileClasspath(Project project) {
		final JavaPluginConvention java = project
				.getConvention().findPlugin(JavaPluginConvention.class);
		if (java != null) {
			return java.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME).getCompileClasspath();
		}
		else {
			return project.files(Collections.emptyList());
		}
	}

	private void configureDokkaApiTask(Project project) {
		final DokkaTask dokka = project.getTasks().withType(DokkaTask.class).getByName(KOTLIN_API_TASK_NAME);
		dokka.doFirst(t -> {
			FileCollection subProjectsOutput = project.getSubprojects().stream()
					.map(p -> getJarOutputFiles(p)).reduce((first, second) -> first.plus(second)).get();
			FileCollection subProjectsClasspath = project.getSubprojects().stream()
					.map(p -> getCompileClasspath(p)).reduce((first, second) -> first.plus(second)).get();
			dokka.setClasspath(subProjectsOutput.plus(subProjectsClasspath));
			FileCollection kotlinSourceDirs = getKotlinSourceDirs(project);
			dokka.setSourceDirs(kotlinSourceDirs);
		});
		dokka.dependsOn(project.getTasksByName(JAVADOC_API_TASK_NAME, false));
		dokka.setModuleName("spring-framework");
		dokka.setOutputFormat("html");
		dokka.setOutputDirectory(project.getBuildDir() + "/docs/kdoc");
	}

	private FileCollection getJarOutputFiles(Project project) {
		Jar jar = project.getTasks().withType(Jar.class).getByName("jar");
		return jar.getOutputs().getFiles();
	}

	private FileCollection getKotlinSourceDirs(Project project) {
		return project.getSubprojects().stream()
				.map(p -> (FileCollection) p.files("src/main/kotlin"))
				.reduce(FileCollection::plus)
				.get();
	}

}

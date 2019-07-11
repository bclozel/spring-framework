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

package org.springframework.build.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;

/**
 * {@link Plugin} that applies automatically conventions for compiling
 * Java and Kotlin sources.
 * <p>Once can override the default Java source+target compatibility version
 * with a dedicated property on the CLI: {@code "./gradlew test -PjavaCompatVersion=11"}.
 * 
 * @author Brian Clozel
 */
public class CompileConventionsPlugin implements Plugin<Project> {

	private static List<String> COMPILER_ARGS;

	private static List<String> TEST_COMPILER_ARGS;

	static {
		List<String> commonCompilerArgs = Arrays.asList(
				"-Xlint:serial", "-Xlint:cast", "-Xlint:classfile", "-Xlint:dep-ann",
				"-Xlint:divzero", "-Xlint:empty", "-Xlint:finally", "-Xlint:overrides",
				"-Xlint:path", "-Xlint:processing", "-Xlint:static", "-Xlint:try", "-Xlint:-options"
		);
		COMPILER_ARGS = new ArrayList<>();
		COMPILER_ARGS.addAll(commonCompilerArgs);
		COMPILER_ARGS.addAll(Arrays.asList(
				"-Xlint:serial", "-Xlint:cast", "-Xlint:classfile", "-Xlint:dep-ann",
				"-Xlint:divzero", "-Xlint:empty", "-Xlint:finally", "-Xlint:overrides",
				"-Xlint:path", "-Xlint:processing", "-Xlint:static", "-Xlint:try", "-Xlint:-options"
		));
		TEST_COMPILER_ARGS = new ArrayList<>();
		TEST_COMPILER_ARGS.addAll(commonCompilerArgs);
		TEST_COMPILER_ARGS.addAll(Arrays.asList("-Xlint:-varargs", "-Xlint:-fallthrough", "-Xlint:-rawtypes",
				"-Xlint:-deprecation", "-Xlint:-unchecked", "-parameters"));
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, plugin -> applyJavaCompileConventions(project));
		project.getPlugins().withType(KotlinPluginWrapper.class, plugin -> applyKotlinCompileConventions(project));
	}

	/**
	 * Applies the common Java compiler options for sources and test sources.
	 * @param project the current project
	 */
	private void applyJavaCompileConventions(Project project) {
		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
		if (project.hasProperty("javaCompatVersion")) {
			JavaVersion javaCompatVersion = JavaVersion.toVersion(project.property("javaCompatVersion"));
			java.setSourceCompatibility(javaCompatVersion);
			java.setTargetCompatibility(javaCompatVersion);
		}
		else {
			java.setSourceCompatibility(JavaVersion.VERSION_1_8);
			java.setTargetCompatibility(JavaVersion.VERSION_1_8);
		}

		TaskCollection<JavaCompile> javaCompilers = project.getTasks().withType(JavaCompile.class);
		javaCompilers.forEach(compileTask -> {
			if (compileTask.getName().equals(JavaPlugin.COMPILE_JAVA_TASK_NAME)) {
				compileTask.getOptions().setCompilerArgs(COMPILER_ARGS);
			}
			else if (compileTask.getName().equals(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME)) {
				compileTask.getOptions().setCompilerArgs(TEST_COMPILER_ARGS);
			}
			compileTask.getOptions().setEncoding("UTF-8");
		});
	}

	/**
	 * Applies the common Kotlin compiler options for sources and test sources.
	 * @param project the current project
	 */
	private void applyKotlinCompileConventions(Project project) {
		TaskCollection<KotlinCompile> kotlinCompilers = project.getTasks().withType(KotlinCompile.class);
		kotlinCompilers.forEach(compileTask -> {
			compileTask.getKotlinOptions().setJvmTarget("1.8");
			compileTask.getKotlinOptions().setFreeCompilerArgs(Collections.singletonList("-Xjsr305=strict"));
		});
	}
}

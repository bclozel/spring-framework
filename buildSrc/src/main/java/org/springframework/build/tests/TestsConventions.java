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

package org.springframework.build.tests;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.testing.Test;

/**
 * @{link Plugin} that applies Java test conventions to projects.
 * 
 * @author Brian Clozel
 */
public class TestsConventions implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, plugin -> applyTestsConventions(project));
	}

	private void applyTestsConventions(Project project) {
		project.getTasks().withType(Test.class, test -> {
			test.systemProperty("java.awt.headless", "true");
			test.systemProperty("testGroups", project.getProperties().get("testGroups"));
			test.systemProperty("io.netty.leakDetection.level", "paranoid");
			test.useJUnitPlatform();
			test.setScanForTestClasses(false);
			test.include("**/*Tests.class", "**/*Test.class");
			// Since we set scanForTestClasses to false, we need to filter out inner
			// classes with the "$" pattern; otherwise, using -Dtest.single=MyTests to
			// run MyTests by itself will fail if MyTests contains any inner classes.
			test.exclude("**/Abstract*.class", "**/*$*");
		});
	}
}

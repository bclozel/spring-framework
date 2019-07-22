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

import java.io.File;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.jvm.tasks.Jar;

/**
 * @author Brian Clozel
 */
public class JarConventionsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		if(project != project.getRootProject()) {
			project.getPlugins().withType(JavaPlugin.class, java -> configureJarConventions(project));
		}
	}

	private void configureJarConventions(Project project) {
		project.getTasks().withType(Jar.class, jar -> {
			 jar.manifest(manifest -> {
				 Map<String, String> attributes = new HashMap<>();
				 attributes.put("Implementation-Title", project.getName());
				 attributes.put("Implementation-Version", project.getVersion().toString());
				 // for Jigsaw
				 attributes.put("Automatic-Module-Name", project.getName().replace('-', '.'));
				 attributes.put("Created-By", System.getProperty("java.version")
				 + " (" + System.getProperty("java.specification.vendor") +")");
			 	 manifest.attributes(attributes);
			 });

			 Map<String, String> expand = new HashMap<>();
			 expand.put("copyright", Year.now().toString());
			 expand.put("version", project.getVersion().toString());
			jar.from(new File(project.getRootDir() + "src/docs/dist"))
					.include("license.txt", "notice.txt")
					.into("META-INF")
					.expand(expand);
		});
	}
}

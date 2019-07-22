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

package org.springframework.build.publish;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * @author Brian Clozel
 */
public class MavenPublishConventions implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(MavenPublishPlugin.class);
		project.getPlugins().withType(MavenPublishPlugin.class, plugin -> customizePublication(project));
		if(project == project.getRootProject()) {
			project.getPlugins().withType(MavenPublishPlugin.class, plugin -> publishRootArtifacts(project));
		}
		else {
			project.getPlugins().withType(MavenPublishPlugin.class, plugin -> publishModuleArtifacts(project));
		}
	}

	private void customizePublication(Project project) {
		PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
		publishing.getPublications().withType(MavenPublication.class, publication -> {
			MavenPom pom = publication.getPom();
			pom.getUrl().set("https://github.com/spring-projects/spring-framework");
			pom.organization(org -> {
				org.getName().set("Spring IO");
				org.getUrl().set("https://spring.io/projects/spring-framework");
			});
			pom.licenses(licenses -> {
				licenses.license(l -> {
					l.getName().set("Apache License, Version 2.0");
					l.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0");
					l.getDistribution().set("repo");
				});
			});
			pom.scm(scm -> {
				scm.getUrl().set("https://github.com/spring-projects/spring-framework");
				scm.getConnection().set("scm:git:git://github.com/spring-projects/spring-framework");
				scm.getDeveloperConnection().set("scm:git:git://github.com/spring-projects/spring-framework");
			});
			pom.developers(devs -> {
				devs.developer(dev -> {
					dev.getId().set("jhoeller");
					dev.getName().set("Juergen Hoeller");
					dev.getEmail().set("jhoeller@pivotal.io");
				});
			});
			pom.issueManagement(issues -> {
				issues.getSystem().set("GitHub");
				issues.getUrl().set("https://github.com/spring-projects/spring-framework/issues");
			});
		});
	}

	private void publishRootArtifacts(Project project) {
		// Don't publish the default jar for the root project
		Configuration archives = project.getConfigurations().getByName("archives");
		archives.getArtifacts().clear();
		project.afterEvaluate(p -> {
			p.getArtifacts().add("archives", p.getTasks().findByName("docsZip"));
			p.getArtifacts().add("archives",  p.getTasks().findByName("schemaZip"));
			p.getArtifacts().add("archives",  p.getTasks().findByName("distZip"));
		});
	}

	private void publishModuleArtifacts(Project project) {
		project.afterEvaluate(p -> {
			p.getArtifacts().add("archives", p.getTasks().findByName("sourcesJar"));
			p.getArtifacts().add("archives",  p.getTasks().findByName("javadocJar"));
		});
	}

}

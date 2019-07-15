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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.jetbrains.dokka.DokkaConfiguration;
import org.jetbrains.dokka.gradle.DokkaTask;

/**
 * {@link Plugin} that applies common conventions for Spring Framework Javadoc and Dokka tasks.
 * <p>This can be used for both {@code "-javadoc.jar"} artifacts and the Javadocs
 * published on the official website.
 *
 * @author Brian Clozel
 */
public class JavadocConventions implements Plugin<Project> {

	private static final List<String> LINKS = Arrays.asList(
			"https://docs.oracle.com/javase/8/docs/api/",
			"https://javaee.github.io/javaee-spec/javadocs/",
			"https://docs.oracle.com/cd/E13222_01/wls/docs90/javadocs/",  // CommonJ
			"https://docs.jboss.org/jbossas/javadoc/4.0.5/connector/",
			"https://docs.jboss.org/jbossas/javadoc/7.1.2.Final/",
			"https://www.eclipse.org/aspectj/doc/released/aspectj5rt-api/",
			"https://www.ehcache.org/apidocs/3.7.0",
			"https://www.quartz-scheduler.org/api/2.3.0/",
			"https://fasterxml.github.io/jackson-core/javadoc/2.9/",
			"https://fasterxml.github.io/jackson-databind/javadoc/2.9/",
			"https://fasterxml.github.io/jackson-dataformat-xml/javadoc/2.9/",
			"https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/",
			"https://junit.org/junit4/javadoc/4.12/",
			"https://junit.org/junit5/docs/5.5.0/api/"
	);

	private static final List<String> KOTLIN_LINKS = Arrays.asList(
			"https://projectreactor.io/docs/core/release/api/",
			"https://www.reactive-streams.org/reactive-streams-1.0.1-javadoc/",
			"https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"
	);


	@Override
	public void apply(Project project) {
		project.getTasks().withType(Javadoc.class).forEach(t -> {
			final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) t.getOptions();
			options.setDocTitle(project.getDescription() + " " + project.getVersion() + " API");
			options.setHeader(project.getDisplayName());
			options.setMemberLevel(JavadocMemberLevel.PROTECTED);
			options.setAuthor(true);
			options.setUse(true);
			options.setLinks(LINKS);
			options.setSplitIndex(true);
			options.addBooleanOption("Xdoclint:none", true);
			options.setStylesheetFile(project.getRootProject().file("src/docs/api/stylesheet.css"));
			options.setOverview(project.getRootProject().file("src/docs/api/overview.html").getPath());
		});

		project.getTasks().withType(DokkaTask.class).forEach(t -> {
			List<DokkaConfiguration.ExternalDocumentationLink> links = new ArrayList<>();
			KOTLIN_LINKS.forEach(l -> createDocLink(l, null));
			DokkaConfiguration.ExternalDocumentationLink spring =
					createDocLink("https://docs.spring.io/spring-framework/docs/" + project.getVersion() + "/javadoc-api/",
							new File("file://" + project.getBuildDir(), "api/package-list").getPath());
			links.add(spring);
			t.setExternalDocumentationLinks(links);
		});
	}

	private DokkaConfiguration.ExternalDocumentationLink createDocLink(String linkUrl, String listUrl) {
		DokkaConfiguration.ExternalDocumentationLink.Builder builder =
				new DokkaConfiguration.ExternalDocumentationLink.Builder();
		try {
			builder.setUrl(new URL(linkUrl));
			if (listUrl != null) {
				builder.setPackageListUrl(new URL(listUrl));
			}
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return builder.build();
	}
}

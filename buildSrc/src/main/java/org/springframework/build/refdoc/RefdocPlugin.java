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

package org.springframework.build.refdoc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.undercouch.gradle.tasks.download.Download;
import de.undercouch.gradle.tasks.download.DownloadTaskPlugin;
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin;
import org.asciidoctor.gradle.jvm.AsciidoctorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;

/**
 * @author Brian Clozel
 */
public class RefdocPlugin implements Plugin<Project> {

	public static final String REFDOC_TASK_NAME = "refdoc";

	private static final String SPRING_DOC_RESOURCES_VERSION = "0.1.2.RELEASE";

	private static final String SPRING_DOC_RESOURCES_ZIP_URL =
			"https://repo.spring.io/release/io/spring/docresources/spring-doc-resources/" +
					SPRING_DOC_RESOURCES_VERSION +
					"/spring-doc-resources-" + SPRING_DOC_RESOURCES_VERSION + ".zip";

	private static final String ASCIIDOC_ROOT_FOLDER = "/src/docs/asciidoc";

	private static final String BUILD_DOCS_ROOT_FOLDER = "/docs/";

	private static final String DOCS_RESOURCES_FOLDER = "/docs/spring-docs-resources/";

	private static final Map<String, String> DEFAULT_ASCIIDOC_ATTRIBUTES;

	static {
		DEFAULT_ASCIIDOC_ATTRIBUTES = new HashMap<>();
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("icons", "font");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("idprefix", "");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("idseparator", "-");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("docinfo", "shared");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("setanchors", "");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("sectnums", "");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("source-highlighter", "highlight.js");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("highlightjsdir", "js/highlight");
		DEFAULT_ASCIIDOC_ATTRIBUTES.put("highlightjs-theme", "atom-one-dark-reasonable");
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(DownloadTaskPlugin.class);
		project.getPluginManager().apply(AsciidoctorJBasePlugin.class);

		createRefdocTask(project.getRootProject());
	}

	private void createRefdocTask(Project project) {
		Download downloadDocResources = downloadResources(project);

		Copy copyDocResources = copryRresources(project, downloadDocResources.getDest());
		copyDocResources.dependsOn(downloadDocResources);

		AsciidoctorTask refdocs = refdoc(project, copyDocResources.getDestinationDir());
		refdocs.dependsOn(copyDocResources);
	}


	private Download downloadResources(Project project) {
		Download downloadDocResources = project.getTasks().create("downloadDocResources", Download.class);
		downloadDocResources.src(SPRING_DOC_RESOURCES_ZIP_URL);
		downloadDocResources.dest(new File(project.getBuildDir(), BUILD_DOCS_ROOT_FOLDER + "spring-doc-resources.zip"));
		return downloadDocResources;
	}

	private Copy copryRresources(Project project, File inputFolder) {
		Copy copyDocResources = project.getTasks().create("copyDocResources", Copy.class);
		copyDocResources.from(project.zipTree(inputFolder));
		copyDocResources.into(new File(project.getBuildDir(), DOCS_RESOURCES_FOLDER));
		return copyDocResources;
	}

	private AsciidoctorTask refdoc(Project project, File docsResourcesFolder) {
		AsciidoctorTask refdocs = project.getTasks().create(REFDOC_TASK_NAME, AsciidoctorTask.class);
		refdocs.setSourceDir(new File(project.getRootDir(), ASCIIDOC_ROOT_FOLDER));
		refdocs.sources(pattern -> pattern.include("*.adoc"));
		refdocs.baseDirFollowsSourceDir();
		refdocs.resources(copySpec -> {
			copySpec.from(refdocs.getSourceDir()).include("images/*", "css/**", "js/**");
			copySpec.from(docsResourcesFolder);
		});
		refdocs.outputOptions(opt -> {
			opt.backends("html5");
		});

		Map<String, String> options = new HashMap<>();
		options.put("doctype", "book");
		options.put("eruby", "erubis");
		refdocs.setOptions(options);

		Map<String, String> attributes = new HashMap<>(DEFAULT_ASCIIDOC_ATTRIBUTES);
		attributes.put("revnumber", project.getVersion().toString());
		attributes.put("spring-version", project.getVersion().toString());
		attributes.put("stylesdir", docsResourcesFolder.getAbsolutePath() + "/css/");
		attributes.put("docinfodir", docsResourcesFolder.getAbsolutePath());
		attributes.put("stylesheet", "spring.css");
		refdocs.setAttributes(attributes);
		return refdocs;
	}
}
